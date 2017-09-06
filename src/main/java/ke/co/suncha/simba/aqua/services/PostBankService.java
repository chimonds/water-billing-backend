package ke.co.suncha.simba.aqua.services;

import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.models.PostBankTransaction;
import ke.co.suncha.simba.aqua.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Created by manyala on 6/11/15.
 */
@Service
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
    AccountManagerService accountService;

    @Autowired
    private AccountStatusHistoryRepository accountStatusHistoryRepository;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public PostBankService() {

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
