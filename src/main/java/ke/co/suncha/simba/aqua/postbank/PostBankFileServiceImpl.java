package ke.co.suncha.simba.aqua.postbank;

import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.postbank.transaction.PostBankTransactionRepository;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.repository.BillingMonthRepository;
import ke.co.suncha.simba.aqua.repository.PaymentSourceRepository;
import ke.co.suncha.simba.aqua.repository.PaymentTypeRepository;
import ke.co.suncha.simba.aqua.services.AccountService;
import ke.co.suncha.simba.aqua.services.PaymentService;
import ke.co.suncha.simba.aqua.services.SMSService;
import ke.co.suncha.simba.aqua.utils.SMSNotificationType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;

/**
 * Created by maitha.manyala on 6/21/16.
 */
@Service
public class PostBankFileServiceImpl implements PostBankFileService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PostBankFileRepository postBankFileRepository;

    @Autowired
    private SimbaOptionService optionService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuthManager authManager;

    private RestResponse response;

    private RestResponseObject responseObject = new RestResponseObject();

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PostBankTransactionRepository postBankTransactionRepository;

    @Autowired
    PaymentService paymentService;

    @Autowired
    AccountService accountService;

    @Autowired
    BillingMonthRepository billingMonthRepository;

    @Autowired
    PaymentTypeRepository paymentTypeRepository;

    @Autowired
    PaymentSourceRepository paymentSourceRepository;

    @Autowired
    SMSService smsService;

    @Override
    @Transactional
    public RestResponse post(RestRequestObject<PostBankFile> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "postBank_postFile");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                PostBankFile postBankFile = requestObject.getObject();
                if (postBankFile == null) {
                    responseObject.setMessage("Invalid file resource");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                PostBankFile dbPostBankFile = postBankFileRepository.findOne(postBankFile.getFileId());
                if (dbPostBankFile == null) {
                    responseObject.setMessage("Invalid file resource");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                if (dbPostBankFile.getStatus() == FileStatus.POSTED) {
                    responseObject.setMessage("Sorry we can not complete your request, file has already been posted.");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                if (dbPostBankFile.getStatus() == FileStatus.VOIDED) {
                    responseObject.setMessage("Sorry we can not complete your request, file has already been voided.");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                if (dbPostBankFile.getStatus() == FileStatus.DRAFT) {

                    if (postBankFile.getStatus() == FileStatus.VOIDED) {
                        dbPostBankFile.setStatus(FileStatus.VOIDED);
                        dbPostBankFile.setIsValid(Boolean.FALSE);
                        dbPostBankFile.setAmount(0d);
                        dbPostBankFile.setLineCount(0);
                        dbPostBankFile = postBankFileRepository.save(dbPostBankFile);

                        List<PostBankTransaction> postBankTransactions = postBankTransactionRepository.findAllByPostBankFile(dbPostBankFile);
                        if (!postBankTransactions.isEmpty()) {
                            for (PostBankTransaction transaction : postBankTransactions) {
                                postBankTransactionRepository.delete(transaction);
                            }
                        }
                        responseObject.setMessage("File voided successfully.");
                        response = new RestResponse(responseObject, HttpStatus.OK);
                        return response;
                    }

                    if (dbPostBankFile.getLineCount() == 0) {
                        responseObject.setMessage("Sorry we can not complete your request, your document is empty");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }

                    if (!dbPostBankFile.getIsValid()) {
                        responseObject.setMessage("Sorry we can not complete your request, you have some invalid line items");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }

                    if (postBankFile.getStatus() != FileStatus.POSTED) {
                        responseObject.setMessage("Sorry we can not complete your request, invalid file action");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }


                    BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);
                    if (billingMonth == null) {
                        responseObject.setMessage("Sorry we can not complete your request, you do not have open billing month");
                        response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                        return response;
                    }

                    dbPostBankFile.setStatus(FileStatus.POSTED);
                    dbPostBankFile.setLastModifiedDate(Calendar.getInstance());
                    dbPostBankFile = postBankFileRepository.save(dbPostBankFile);

                    //allocate transactions
                    List<PostBankTransaction> postBankTransactions = postBankTransactionRepository.findAllByPostBankFile(dbPostBankFile);
                    if (!postBankTransactions.isEmpty()) {
                        for (PostBankTransaction transaction : postBankTransactions) {
                            try {
                                //get account
                                Account account = accountRepository.findByaccNo(transaction.getAccNo());

                                if (account == null) {
                                    log.error("Invalid Post Bank account no for transaction:" + transaction.getAccNo());
                                }

                                if (account != null) {
                                    // update account balance
                                    accountService.setUpdateBalance(account.getAccountId());
                                    accountService.updateBalance(account.getAccountId());

                                    Payment payment = new Payment();
                                    payment.setAccount(account);
                                    payment.setReceiptNo("PB_" + transaction.getSeqNo());
                                    payment.setAmount(transaction.getPaidAmount());

                                    //transaction date
                                    payment.setTransactionDate(Calendar.getInstance());

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

                                    if (paymentType != null) {
                                        payment.setPaymentType(paymentType);
                                    }

                                    PaymentSource paymentSource = paymentSource = paymentSourceRepository.findByName("PostBank");
                                    if (paymentSource != null) {
                                        payment.setPaymentSource(paymentSource);
                                    }

                                    //check if receipt exists
                                    if (payment.getAccount() != null && payment.getBillingMonth() != null && payment.getPaymentType() != null & payment.getPaymentSource() != null && payment.getAmount() > 0) {
                                        //check if receipt no exists
                                        if (paymentService.exits(payment.getReceiptNo())) {
                                            log.error("Post Bank transaction " + payment.getReceiptNo() + " already exists");
                                        } else {
                                            //
                                            Payment created = paymentService.create(payment, account.getAccountId());
                                            //Payment created = paymentRepository.save(payment);
                                            log.info("Assigned Post Bank payment " + payment.getReceiptNo() + " to " + account.getAccNo());

                                            //TODO;
                                            //send message to customer if real account found
                                            smsService.saveNotification(account.getAccountId(), created.getPaymentid(), 0L, SMSNotificationType.PAYMENT);
                                        }

                                        //Mark Post transaction as assigned
                                        transaction.setAssigned(Boolean.TRUE);
                                        transaction.setAllocated(1);
                                        transaction.setDateAssigned(Calendar.getInstance());
                                        transaction.setAccount(account);
                                        postBankTransactionRepository.save(transaction);

                                        accountService.setUpdateBalance(account.getAccountId());
                                        accountService.updateBalance(account.getAccountId());
                                    }
                                }

                            } catch (Exception ex) {
                                log.error(ex.getMessage());
                            }
                        }
                    }

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(postBankFile.getFileId()));
                    auditRecord.setParentObject("PostBankFile");
                    auditRecord.setCurrentData(postBankFile.toString());
                    auditRecord.setNotes("POSTED POSTBANK FILE");
                    auditService.log(AuditOperation.CREATED, auditRecord);
                    //End - audit trail

                    // package response
                    responseObject.setMessage("File posted successfully. ");
                    responseObject.setPayload(postBankFile);
                    response = new RestResponse(responseObject, HttpStatus.CREATED);
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @Override
    @Transactional
    public RestResponse create(String token, MultipartFile file) {
        try {
            response = authManager.tokenValid(token);
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(token, "postBank_uploadfile");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                if (file.isEmpty()) {
                    responseObject.setMessage("File can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                } else if (file.getContentType().compareToIgnoreCase("text/csv") != 0) {
                    responseObject.setMessage("Sorry we can not process your request. We only allow text/csv files");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                } else {
                    PostBankFile postBankFile = new PostBankFile();
                    postBankFile.setName(file.getOriginalFilename());
                    postBankFile.setSize(file.getSize());
                    postBankFile = postBankFileRepository.save(postBankFile);

                    //read line items
                    InputStream is = file.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String content;
                    Integer count = 0;
                    Boolean fileValid = Boolean.TRUE;
                    Double totalAmount = 0d;
                    while ((content = br.readLine()) != null) {
                        if (count > 0) {
                            String[] line = content.split(",");

                            if (line.length == 10) {
                                PostBankTransaction postBankTransaction = new PostBankTransaction();
                                postBankTransaction.setSeqNo(StringUtils.trim(line[0]));
                                postBankTransaction.setAccNo(StringUtils.trim(line[8]));
                                postBankTransaction.setPayeeNames(StringUtils.trim(line[9]));
                                postBankTransaction.setPaidAmount(Double.parseDouble(StringUtils.trim(line[6])));
                                Boolean accValid = Boolean.TRUE;
                                Boolean receiptValid = Boolean.TRUE;

                                //Check if account is valid
                                Account account = accountRepository.findByaccNo(postBankTransaction.getAccNo());
                                if (account == null) {
                                    accValid = Boolean.FALSE;
                                }

                                if (paymentService.exits(postBankTransaction.getSeqNo()) || paymentService.exits("PB_" + postBankTransaction.getSeqNo())) {
                                    receiptValid = Boolean.FALSE;
                                }
                                Boolean include = true;
                                if (postBankTransactionRepository.findBySeqNo(postBankTransaction.getSeqNo()) != null) {
                                    receiptValid = Boolean.FALSE;
                                    include = false;
                                }

                                //Check if receipt number exists
                                if (!accValid || !receiptValid) {
                                    fileValid = Boolean.FALSE;
                                }
                                totalAmount += postBankTransaction.getPaidAmount();
                                postBankTransaction.setAccountValid(accValid);
                                postBankTransaction.setReceiptValid(receiptValid);
                                postBankTransaction.setPostBankFile(postBankFile);
                                postBankTransactionRepository.save(postBankTransaction);
                            } else {
                                fileValid = false;
                            }
                        }
                        count++;
                    }
                    br.close();

                    if (totalAmount <= 0) {
                        fileValid = Boolean.FALSE;
                    }

                    postBankFile.setIsValid(fileValid);
                    postBankFile.setAmount(totalAmount);
                    postBankFile.setLineCount(count--);
                    postBankFile = postBankFileRepository.save(postBankFile);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(postBankFile.getFileId()));
                    auditRecord.setParentObject("PostBankFile");
                    auditRecord.setCurrentData(postBankFile.toString());
                    auditRecord.setNotes("CREATED POSTBANK FILE");
                    auditService.log(AuditOperation.CREATED, auditRecord);
                    //End - audit trail

                    // package response
                    responseObject.setMessage("File created successfully. ");
                    responseObject.setPayload(postBankFile);
                    response = new RestResponse(responseObject, HttpStatus.CREATED);
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    @Override
    public RestResponse findAll(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "postBank_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                RestPageRequest p = requestObject.getObject();
                Page<PostBankFile> page;
                if (p.getFilter().isEmpty()) {
                    page = postBankFileRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                } else {
                    page = postBankFileRepository.findAllByNameContains(p.getFilter(), new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                }
                if (page.hasContent()) {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(page);
                    response = new RestResponse(responseObject, HttpStatus.OK);
                } else {
                    responseObject.setMessage("Your search did not match any records");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @Override
    public RestResponse findTransactionsByFile(RestRequestObject<PostBankFile> requestObject, Long fileId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "postBank_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                PostBankFile dbFile = postBankFileRepository.findOne(fileId);
                if (dbFile == null) {
                    responseObject.setMessage("Invalid file resource");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                }

                List<PostBankTransaction> postBankTransactions = postBankTransactionRepository.findAllByPostBankFile(dbFile);

                if (!postBankTransactions.isEmpty()) {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(postBankTransactions);
                    response = new RestResponse(responseObject, HttpStatus.OK);
                } else {
                    responseObject.setMessage("Your search did not match any records");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @Override
    public RestResponse findOne(RestRequestObject<PostBankFile> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "postBank_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                PostBankFile postBankFile = requestObject.getObject();

                if (postBankFile == null) {
                    responseObject.setMessage("Invalid file resource");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                }

                PostBankFile dbFile = postBankFileRepository.findOne(postBankFile.getFileId());

                if (dbFile == null) {
                    responseObject.setMessage("Invalid file resource");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                }

                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(dbFile);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }
}
