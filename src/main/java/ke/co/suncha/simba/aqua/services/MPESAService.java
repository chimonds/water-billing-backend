package ke.co.suncha.simba.aqua.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import ke.co.suncha.simba.admin.repositories.SimbaOptionRepository;
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
import ke.co.suncha.simba.aqua.utils.SMSNotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;
import java.util.List;

/**
 * Created by manyala on 6/6/15.
 */
@Service
public class MPESAService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    MPESARepository mpesaRepository;

    @Autowired
    PaymentTypeRepository paymentTypeRepository;

    @Autowired
    BillingMonthRepository billingMonthRepository;

    @Autowired
    PaymentSourceRepository paymentSourceRepository;

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    SimbaOptionRepository optionRepository;

    @Autowired
    SMSService smsService;

    @Autowired
    AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    SimbaOptionService optionService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    AuditService auditService;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountStatusHistoryRepository accountStatusHistoryRepository;

    //@Autowired
    // MbassadorService mbassadorService;

    private RestResponse response;

    private RestResponseObject responseObject = new RestResponseObject();

    @Scheduled(fixedDelay = 5000)
    public void pollMPESATransactions() {
        pollTransactions();
    }

    @Transactional
    private void pollTransactions() {
        //optionService = new SimbaOptionService();
        try {
            Boolean pollMpesaTransactions = Boolean.FALSE;
            try {
                String b = optionService.getOption("MPESA_ENABLE").getValue();
                pollMpesaTransactions = Boolean.parseBoolean(b);
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
            if (!pollMpesaTransactions) {
                return;
            }

            RestTemplate restTemplate = new RestTemplate();
            MPESARequest mpesaRequest = new MPESARequest();
            mpesaRequest.setPay_bill(optionService.getOption("MPESA_PAYBILL").getValue());
            mpesaRequest.setKey(optionService.getOption("MPESA_KEY").getValue());

            String url = optionService.getOption("MPESA_TRANSACTIONS_ENDPOINT").getValue();
            String jsonResponse = restTemplate.postForObject(url, mpesaRequest, String.class);

            //2. Convert JSON to Java object
            ObjectMapper mapper = new ObjectMapper();
            MPESAResponse mpesaResponse = mapper.readValue(jsonResponse, MPESAResponse.class);

            if (!mpesaResponse.getError()) {
                if (!mpesaResponse.getPayload().isEmpty()) {
                    for (MPESATransaction mpesaTransaction : mpesaResponse.getPayload()) {
                        try {
                            //save transaction
                            MPESATransaction mpesaTransaction1 = mpesaRepository.findByMpesacode(mpesaTransaction.getMpesacode());
                            if (mpesaTransaction1 == null) {
                                mpesaTransaction = mpesaRepository.save(mpesaTransaction);

                                //Notify event bus to allocate transaction
                                //mbassadorService.bus.publishAsync(mpesaTransaction);
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
                            log.error(ex.getMessage());
                        }
                    }
                }
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void doAllocations() {
        allocate();
    }

    @Transactional
    private void allocate() {
        List<MPESATransaction> mpesaTransactions = mpesaRepository.findAllByAssigned(Boolean.FALSE);
        if (!mpesaTransactions.isEmpty()) {
            for (MPESATransaction transaction : mpesaTransactions) {
                allocateTransaction(transaction);
            }
        }
    }

    @Transactional
    public void allocateTransaction(MPESATransaction mt) {
        try {
            if (mt == null) {
                return;
            }
            MPESATransaction mpesaTransaction = mpesaRepository.findOne(mt.getRecordId());

            if (mpesaTransaction == null) {
                return;
            }
            if (mpesaTransaction.getAssigned()) {
                return;
            }
            try {

                if (mpesaTransaction.getMpesaacc() == null) {
                    mpesaTransaction.setMpesaacc("");
                }

                if (mpesaTransaction.getMpesaamt() != null) {
                    if (mpesaTransaction.getMpesaamt() <= 0) {
                        //notify and delete transactions
                        mpesaRepository.delete(mpesaTransaction);
                    }
                }

                //get account
                Account account = accountRepository.findByaccNo(mpesaTransaction.getMpesaacc().trim());

                if (account == null) {
                    //log.error("Invalid MPESA account no for transaction:" + mpesaTransaction.getText());
                }

                if (account != null) {
                    // update account balance
                    accountService.setUpdateBalance(account.getAccountId());
                    accountService.updateBalance(account.getAccountId());

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

                    PaymentType paymentType = null;

                    Boolean smartReceipting = Boolean.parseBoolean(optionService.getOption("AUTO_SMART_RECEIPTING").getValue());

                    if (smartReceipting) {
                        paymentType = paymentTypeRepository.findByName("Smart Receipt");
                    } else {
                        paymentType = paymentTypeRepository.findByName("Water Sale");
                    }

                    log.info("SMART RECEIPTING ON MPESA:"+ smartReceipting);

                    if (paymentType != null) {
                        payment.setPaymentType(paymentType);
                    }

                    PaymentSource paymentSource = paymentSource = paymentSourceRepository.findByName("M-PESA");
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

                            //
                            Payment created = paymentService.create(payment, account.getAccountId());
                            //Payment created = paymentRepository.save(payment);
                            log.info("Assigned M-PESA payment " + payment.getReceiptNo() + " to " + account.getAccNo());

                            accountService.setUpdateBalance(account.getAccountId());
                            accountService.updateBalance(account.getAccountId());

                            //TODO;
                            //send message to customer if real account found
                            smsService.saveNotification(account.getAccountId(), created.getPaymentid(), 0L, SMSNotificationType.PAYMENT);
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
                response = authManager.grant(requestObject.getToken(), "mpesa_view");
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
                        mpesaTransaction = mpesaRepository.save(mpesaTransaction);

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
