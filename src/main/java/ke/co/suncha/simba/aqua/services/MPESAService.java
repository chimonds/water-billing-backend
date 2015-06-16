package ke.co.suncha.simba.aqua.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.repository.*;
import ke.co.suncha.simba.aqua.utils.MPESARequest;
import ke.co.suncha.simba.aqua.utils.MPESAResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by manyala on 6/6/15.
 */
@Service
public class MPESAService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MPESARepository mpesaRepository;

    @Autowired
    private PaymentTypeRepository paymentTypeRepository;

    @Autowired
    private BillingMonthRepository billingMonthRepository;

    @Autowired
    private PaymentSourceRepository paymentSourceRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SimbaOptionService optionService;

    @Autowired
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AccountStatusHistoryRepository accountStatusHistoryRepository;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public MPESAService() {

    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    private void pollTransactions() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            MPESARequest mpesaRequest = new MPESARequest();
            mpesaRequest.setPay_bill(optionService.getOption("MPESA_PAYBILL").getValue());
            mpesaRequest.setKey(optionService.getOption("MPESA_KEY").getValue());

            String url = optionService.getOption("MPESA_TRANSACTIONS_ENDPOINT").getValue();
            String jsonResponse = restTemplate.postForObject(url, mpesaRequest, String.class);

            //2. Convert JSON to Java object
            ObjectMapper mapper = new ObjectMapper();
            MPESAResponse mpesaResponse = mapper.readValue(jsonResponse, MPESAResponse.class);

            log.info("MPESA:" + mpesaResponse.getMessage());
            if (!mpesaResponse.getError()) {
                if (!mpesaResponse.getPayload().isEmpty()) {
                    for (MPESATransaction mpesaTransaction : mpesaResponse.getPayload()) {
                        try {
                            //save transaction
                            MPESATransaction mpesaTransaction1 = mpesaRepository.findByMpesacode(mpesaTransaction.getMpesacode());
                            if (mpesaTransaction1 == null) {
                                mpesaTransaction = mpesaRepository.save(mpesaTransaction);
                            }

                            url = optionService.getOption("MPESA_UPDATE_TRANSACTION_ENDPOINT").getValue();
                            mpesaRequest.setRecord_id(mpesaTransaction.getId().toString());
                            jsonResponse = restTemplate.postForObject(url, mpesaRequest, String.class);
                            mpesaResponse = mapper.readValue(jsonResponse, MPESAResponse.class);
                            if (!mpesaResponse.getError()) {
                                mpesaTransaction.setNotified(true);
                                mpesaTransaction = mpesaRepository.save(mpesaTransaction);
                            }

                        } catch (Exception ex) {

                        }
                    }
                }
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    private void allocateTransactions() {
        try {
            List<MPESATransaction> mpesaTransactions = mpesaRepository.findAllByAssigned(false);
            if (!mpesaTransactions.isEmpty()) {

                log.info("Assigning " + mpesaTransactions.size() + " M-PESA transactions to accounts");
                for (MPESATransaction mpesaTransaction : mpesaTransactions) {
                    try {

                        if (mpesaTransaction.getMpesaacc()==null) {
                            mpesaTransaction.setMpesaacc("");
                        }




                        //get account
                        Account account = accountRepository.findByaccNo(mpesaTransaction.getMpesaacc().trim());

                        if (account == null) {
                            log.error("Invalid MPESA account no for transaction:" + mpesaTransaction.getText());
                        }

                        if (account != null) {
                            Payment payment = new Payment();
                            payment.setAccount(account);
                            payment.setReceiptNo(mpesaTransaction.getMpesacode());
                            payment.setAmount(mpesaTransaction.getMpesaamt());

                            //transaction date
                            payment.setTransactionDate(Calendar.getInstance());

                            BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);
                            if (billingMonth != null) {
                                payment.setBillingMonth(billingMonth);
                            }


                            PaymentType paymentType = paymentTypeRepository.findByName("Water Sale");
                            if (paymentType != null) {
                                payment.setPaymentType(paymentType);
                            }

                            PaymentSource paymentSource = paymentSourceRepository.findByName("M-PESA");
                            if (paymentSource != null) {
                                payment.setPaymentSource(paymentSource);
                            }

                            payment.setNotes(mpesaTransaction.getText());

                            //check if receipt exists

                            //check if amount is >0, payment type !=null, billing month !=null, account !=null
                            if (payment.getAccount() != null && payment.getBillingMonth() != null && payment.getPaymentType() != null & payment.getPaymentSource() != null && payment.getAmount() > 0) {
                                //check if receipt no exists
                                Payment payment1 = paymentRepository.findByreceiptNo(payment.getReceiptNo());
                                if (payment1 != null) {
                                    log.error("MPESA transaction " + payment.getReceiptNo() + " already exists");
                                } else if (payment1 == null) {

                                    Payment created = paymentRepository.save(payment);
                                    log.info("Assigned M-PESA payment " + payment.getReceiptNo() + " to " + account.getAccNo());

                                    // update account balance
                                    account.setOutstandingBalance(paymentService.getAccountBalance(account.getAccountId()));
                                    accountRepository.save(account);

                                    //TODO;
                                    //send message to customer if real account found
                                }

                                //Mark MPESA transaction as assigned
                                mpesaTransaction.setAssigned(true);
                                mpesaTransaction.setAllocated(1);
                                mpesaTransaction.setDateAssigned(Calendar.getInstance());
                                mpesaTransaction.setAccount(account);
                                mpesaRepository.save(mpesaTransaction);
                            }
                        }

                    } catch (Exception ex) {
                        log.error(ex.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "MPESA_transactions_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest p = requestObject.getObject();
                Page<MPESATransaction> page;
                if (p.getFilter().isEmpty()) {
                    page = mpesaRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                } else {
                    page = mpesaRepository.findByMpesaaccContainsOrAccount_AccNoContainsOrMpesacodeContains(p.getFilter(), p.getFilter(), p.getFilter(), new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                }
                if (page.hasContent()) {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(page);
                    response = new RestResponse(responseObject, HttpStatus.OK);
                } else {
                    responseObject.setMessage("Your search did not match any records");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }


    public RestResponse allocate(RestRequestObject<MPESATransaction> requestObject, Long id) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "mpesa_allocate");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                MPESATransaction mpesaTransaction = mpesaRepository.findOne(id);

                if (mpesaTransaction == null) {
                    responseObject.setMessage("Transaction not found");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                } else {
                    // setup resource
                    if (mpesaTransaction.getAccount() != null) {
                        responseObject.setMessage("Transaction already allocated to " + mpesaTransaction.getAccount().getAccNo() + ".");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;

                    } else {
                        // update account info
                        MPESATransaction transaction = requestObject.getObject();

                        // set account
                        Account acc = accountRepository.findByaccNo(transaction.getMpesaacc());
                        if (acc == null) {
                            responseObject.setMessage("Invalid account number.");
                            responseObject.setPayload("");
                            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                            return response;
                        }

                        mpesaTransaction.setMpesaacc(acc.getAccNo());
                        mpesaTransaction.setNotes(transaction.getNotes());
                        mpesaRepository.save(mpesaTransaction);

                        responseObject.setMessage("Transaction  allocation updated successfully");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.OK);
                    }
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);

            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

}
