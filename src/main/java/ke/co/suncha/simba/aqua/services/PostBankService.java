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
import ke.co.suncha.simba.aqua.utils.PostBankRequest;
import ke.co.suncha.simba.aqua.utils.PostBankResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;
import java.util.List;

/**
 * Created by manyala on 6/11/15.
 */
@Service
@Scope("singleton")
public class PostBankService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PostBankRepository postBankRepository;

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
    AccountService accountService;

    @Autowired
    private AccountStatusHistoryRepository accountStatusHistoryRepository;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public PostBankService() {

    }

    //@Scheduled(fixedDelay = 5000)
    @Transactional
    private void pollTransactions() {
        try {
            RestTemplate restTemplate = new RestTemplate();

            PostBankRequest postBankRequest = new PostBankRequest();
            postBankRequest.setUsername(optionService.getOption("POSTBANK_USERNAME").getValue());
            postBankRequest.setPassword(optionService.getOption("POSTBANK_KEY").getValue());
            String url = optionService.getOption("POSTBANK_TRANSACTIONS_ENDPOINT").getValue();
            String jsonResponse = restTemplate.postForObject(url, postBankRequest, String.class);

            //2. Convert JSON to Java object
            ObjectMapper mapper = new ObjectMapper();

            PostBankResponse response = mapper.readValue(jsonResponse, PostBankResponse.class);


            log.info("POSTBANK:" + response.getMessage());
            if (!response.getError()) {
                if (!response.getPayload().isEmpty()) {
                    for (PostBankTransaction postBankTransaction : response.getPayload()) {
                        try {
                            //save transaction
                            PostBankTransaction postBankTransaction1 = postBankRepository.findBySeqNo(postBankTransaction.getSeqNo());
                            if (postBankTransaction1 == null) {
                                postBankTransaction = postBankRepository.save(postBankTransaction);
                            }

                            url = optionService.getOption("POSTBANK_UPDATE_TRANSACTION_ENDPOINT").getValue();
                            postBankRequest.setRecordId(postBankTransaction.getSeqNo());
                            jsonResponse = restTemplate.postForObject(url, postBankRequest, String.class);

                            response = mapper.readValue(jsonResponse, PostBankResponse.class);
                            if (!response.getError()) {
                                postBankTransaction.setNotified(true);
                                postBankTransaction = postBankRepository.save(postBankTransaction);
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

    //@Scheduled(fixedDelay = 5000)
    @Transactional
    private void allocateTransactions() {
        try {
            List<PostBankTransaction> postBankTransactions = postBankRepository.findAllByAssigned(false);
            if (!postBankTransactions.isEmpty()) {
                log.info("Assigning " + postBankTransactions.size() + " PostBank transactions to accounts");
                for (PostBankTransaction postBankTransaction : postBankTransactions) {
                    try {

                        if (postBankTransaction.getSeqNo() == null) {
                            postBankTransaction.setSeqNo("");
                        }

                        //get account
                        Account account = accountRepository.findByaccNo(postBankTransaction.getAccNo().trim());
                        if (account == null) {
                            log.error("Invalid PostBank account no for transaction:" + postBankTransaction.getSeqNo());
                        }

                        if (account != null) {
                            Payment payment = new Payment();
                            payment.setAccount(account);
                            payment.setReceiptNo(postBankTransaction.getSeqNo());
                            payment.setAmount(postBankTransaction.getPaidAmount()); //TODO; confirm if this is the right amount

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

                            PaymentSource paymentSource = paymentSourceRepository.findByName("PostBank");
                            if (paymentSource != null) {
                                payment.setPaymentSource(paymentSource);
                            }

                            payment.setNotes("Auto posted.");

                            //check if receipt exists

                            //check if amount is >0, payment type !=null, billing month !=null, account !=null
                            if (payment.getAccount() != null && payment.getBillingMonth() != null && payment.getPaymentType() != null & payment.getPaymentSource() != null && payment.getAmount() > 0) {
                                //check if receipt no exists
                                Payment payment1 = paymentRepository.findByreceiptNo(payment.getReceiptNo());
                                if (payment1 != null) {
                                    log.error("PostBank transaction " + payment.getReceiptNo() + " already exists");
                                } else if (payment1 == null) {

                                    Payment created = paymentRepository.save(payment);
                                    log.info("Assigned PostBank payment " + payment.getReceiptNo() + " to " + account.getAccNo());

                                    // update account balance
                                    account.setOutstandingBalance(accountService.getAccountBalance(account.getAccountId()));
                                    accountRepository.save(account);

                                    //TODO;
                                    //send message to customer if real account found
                                }

                                //Mark postbank transaction as assigned

                                postBankTransaction.setAssigned(true);
                                postBankTransaction.setAllocated(1);
                                postBankTransaction.setDateAssigned(Calendar.getInstance());
                                postBankTransaction.setAccount(account);
                                postBankRepository.save(postBankTransaction);
                            }
                        }

                    } catch (Exception ex) {
                        log.error(ex.getMessage());
                        ex.printStackTrace();
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
                response = authManager.grant(requestObject.getToken(), "postbank_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest p = requestObject.getObject();
                Page<PostBankTransaction> page;
                if (p.getFilter().isEmpty()) {
                    page = postBankRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                } else {

                    page = postBankRepository.findBySeqNoContainsOrAccount_AccNoContains(p.getFilter(), p.getFilter(), new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
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


    public RestResponse allocate(RestRequestObject<PostBankTransaction> requestObject, Long id) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "postbank_allocate");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                PostBankTransaction postBankTransaction = postBankRepository.findOne(id);

                if (postBankTransaction == null) {
                    responseObject.setMessage("Transaction not found");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                } else {
                    // setup resource
                    if (postBankTransaction.getAccount() != null) {
                        responseObject.setMessage("Transaction already allocated to " + postBankTransaction.getAccount().getAccNo() + ".");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;

                    } else {
                        // update account info
                        PostBankTransaction transaction = requestObject.getObject();

                        // set account
                        Account acc = accountRepository.findByaccNo(transaction.getAccNo());
                        if (acc == null) {
                            responseObject.setMessage("Invalid account number.");
                            responseObject.setPayload("");
                            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                            return response;
                        }

                        postBankTransaction.setAccNo(acc.getAccNo());
                        postBankTransaction.setNotes(transaction.getNotes());
                        postBankRepository.save(postBankTransaction);

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
