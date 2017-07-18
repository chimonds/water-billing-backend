/*
 * The MIT License
 *
 * Copyright 2015 Maitha Manyala.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ke.co.suncha.simba.aqua.services;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.billing.BillService;
import ke.co.suncha.simba.aqua.makerChecker.tasks.Task;
import ke.co.suncha.simba.aqua.makerChecker.tasks.TaskService;
import ke.co.suncha.simba.aqua.makerChecker.type.TaskTypeConst;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.repository.*;
import ke.co.suncha.simba.aqua.utils.SMSNotificationType;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
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
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Service
public class PaymentService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PaymentTypeRepository paymentTypeRepository;

    @Autowired
    private PaymentSourceRepository paymentSourceRepository;

    @Autowired
    private SMSService smsService;

    @Autowired
    private BillService billService;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountsUpdateRepository accountsUpdateRepository;

    @Autowired
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private SMSRepository smsRepository;

    @Autowired
    MbassadorService mbassadorService;

    @Autowired
    BillingMonthService billingMonthService;

    @Autowired
    TaskService taskService;

    @Autowired
    EntityManager entityManager;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public PaymentService() {

    }

    @Transactional
    public Double getTotalByAccountByDate(Long accountId, DateTime toDate) {
        //toDate = toDate.hourOfDay().withMaximumValue();
        BooleanBuilder where = new BooleanBuilder();
        where.and(QPayment.payment.account.accountId.eq(accountId));
        where.and(QPayment.payment.transactionDate.loe(toDate));
        JPAQuery query = new JPAQuery(entityManager);
        Double dbAmount = query.from(QPayment.payment).where(where).singleResult(QPayment.payment.amount.sum());
        if (dbAmount != null) {
            return dbAmount;
        }
        return 0d;
    }

    @Transactional
    public Double getAccountTotalPayments(Long accountId) {
        Double amount = 0d;
        Account account = accountRepository.findOne(accountId);
        // get payments
        List<Payment> payments = account.getPayments();
        if (!payments.isEmpty()) {
            for (Payment p : payments) {
                if (p.getAmount() > 0) {
                    amount = amount + p.getAmount();
                } else {
                    amount = amount - Math.abs(p.getAmount());
                }
            }
        }
        return amount;
    }

    private Payment getClone(Payment p) {
        Payment payment = new Payment();
        payment.setAmount(p.getAmount());
        payment.setPaymentType(p.getPaymentType());
        payment.setTransactionDate(p.getTransactionDate());
        if (StringUtils.isNotEmpty(p.getNotes())) {
            payment.setNotes(p.getNotes());
        }
        payment.setAccount(p.getAccount());
        payment.setBillingMonth(p.getBillingMonth());
        payment.setPaymentSource(p.getPaymentSource());
        payment.setReceiptNo(p.getReceiptNo());
        return payment;
    }

    //used after user does all validation
    //TODO; clean out
    @Transactional
    public Payment create(Payment payment, Long accountId) {
        Payment pResult = new Payment();
        Account account = accountRepository.findOne(accountId);

        if (account == null || payment == null) {
            return null;
        }

        account = accountRepository.findByaccNo(account.getAccNo());

        if (payment.getBillingMonth() == null || payment.getPaymentType() == null || payment.getPaymentSource() == null || payment.getTransactionDate() == null || StringUtils.isEmpty(payment.getReceiptNo())) {
            return null;
        }

        DateTime transDate = payment.getTransactionDate();
        if (!billingMonthService.canTransact(transDate)) {
            return null;
        }

        BillingMonth billingMonth = billingMonthService.getActiveMonth();
        if (billingMonth == null) {
            return null;
        }
        payment.setBillingMonth(billingMonth);

        PaymentType requestPaymentType = paymentTypeRepository.findOne(payment.getPaymentType().getPaymentTypeId());
        log.info("*******Adding Receipt************");
        log.info("Payment Type:" + requestPaymentType.getName());
        log.info("Payment Source:" + payment.getPaymentSource().getName());

        if (StringUtils.equalsIgnoreCase(requestPaymentType.getName(), "Smart Receipt")) {
            UUID uuid = UUID.randomUUID();
            String randomUUIDString = uuid.toString();
            Double amountToAllocate = payment.getAmount();

            if (account.getPenaltiesBalance() > 0) {
                PaymentType pt = paymentTypeRepository.findByName("Fine");

                //Penalties
                if (amountToAllocate >= account.getPenaltiesBalance()) {
                    amountToAllocate = amountToAllocate - account.getPenaltiesBalance();
                    Payment p = getClone(payment);
                    p.setPaymentType(pt);
                    p.setRefNo(randomUUIDString);
                    p.setIsMultiPart(true);
                    p.setAmount(account.getPenaltiesBalance());
                    pResult = paymentRepository.save(p);
                } else {
                    if (amountToAllocate > 0) {
                        Payment p = getClone(payment);
                        p.setPaymentType(pt);
                        p.setAmount(amountToAllocate);
                        p.setRefNo(randomUUIDString);
                        p.setIsMultiPart(true);
                        pResult = paymentRepository.save(p);
                    }
                    amountToAllocate = 0d;
                }
            }

            if (account.getMeterRentBalance() > 0) {
                PaymentType pt = paymentTypeRepository.findByName("Meter Rent");

                //Meter Rent
                if (amountToAllocate >= account.getMeterRentBalance()) {
                    amountToAllocate = amountToAllocate - account.getMeterRentBalance();
                    Payment p = getClone(payment);
                    p.setPaymentType(pt);
                    p.setAmount(account.getMeterRentBalance());
                    p.setRefNo(randomUUIDString);
                    p.setIsMultiPart(true);
                    pResult = paymentRepository.save(p);
                } else {
                    if (amountToAllocate > 0) {
                        Payment p = getClone(payment);
                        p.setPaymentType(pt);
                        p.setAmount(amountToAllocate);
                        p.setRefNo(randomUUIDString);
                        p.setIsMultiPart(true);
                        pResult = paymentRepository.save(p);
                    }
                    amountToAllocate = 0d;
                }
            }

            //Water sale
            if (account.getWaterSaleBalance() > 0) {
                PaymentType pt = paymentTypeRepository.findByName("Water Sale");
                if (amountToAllocate >= account.getWaterSaleBalance()) {
                    amountToAllocate = amountToAllocate - account.getWaterSaleBalance();
                    Payment p = getClone(payment);
                    p.setPaymentType(pt);
                    p.setAmount(account.getWaterSaleBalance());
                    p.setRefNo(randomUUIDString);
                    p.setIsMultiPart(true);
                    pResult = paymentRepository.save(p);
                } else {
                    if (amountToAllocate > 0) {
                        Payment p = getClone(payment);
                        p.setPaymentType(pt);
                        p.setAmount(amountToAllocate);
                        p.setRefNo(randomUUIDString);
                        p.setIsMultiPart(true);
                        pResult = paymentRepository.save(p);
                    }
                    amountToAllocate = 0d;
                }
            }

            //check if any amount remaining to allocate
            if (amountToAllocate > 0) {
                //check if account is metered
                if (account.getMeter() != null) {
                    if (account.getMeter().getMeterOwner() != null) {
                        if (account.getMeter().getMeterOwner().getCharge()) {
                            if (account.getMeter().getMeterSize() != null) {
                                Double meterRent = account.getMeter().getMeterSize().getRentAmount();
                                PaymentType pt = paymentTypeRepository.findByName("Meter Rent");
                                if (meterRent > 0) {
                                    if (amountToAllocate >= meterRent) {
                                        amountToAllocate = amountToAllocate - meterRent;
                                        Payment p = getClone(payment);
                                        p.setPaymentType(pt);
                                        p.setAmount(meterRent);
                                        p.setRefNo(randomUUIDString);
                                        p.setIsMultiPart(true);
                                        pResult = paymentRepository.save(p);
                                    } else {
                                        amountToAllocate = amountToAllocate - meterRent;
                                        if (amountToAllocate > 0) {
                                            Payment p = getClone(payment);
                                            p.setPaymentType(pt);
                                            p.setAmount(amountToAllocate);
                                            p.setRefNo(randomUUIDString);
                                            p.setIsMultiPart(true);
                                            pResult = paymentRepository.save(p);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //rest of money water sale
            if (amountToAllocate > 0) {
                Payment p = getClone(payment);
                PaymentType pt = paymentTypeRepository.findByName("Water Sale");
                p.setPaymentType(pt);
                p.setAmount(amountToAllocate);
                p.setRefNo(randomUUIDString);
                p.setIsMultiPart(true);
                pResult = paymentRepository.save(p);
            }
        } else {
            Payment p = getClone(payment);
            pResult = paymentRepository.save(p);
        }

        //Update balance
        //accountService.setUpdateBalance(account.getAccountId());
        //accountService.updateBalance(account.getAccountId());

        AccountUpdate accountUpdate = new AccountUpdate();
        accountUpdate.setAccountId(account.getAccountId());
        accountsUpdateRepository.save(accountUpdate);
        return pResult;
    }

    @Transactional
    public void createFromTask(Long taskId) {
        Task task = taskService.getById(taskId);
        if (task == null) {
            return;
        }

        Long accountId = task.getAccount().getAccountId();
        accountService.updateBalance(accountId);
        Account account = accountRepository.findOne(accountId);

        if (account == null) {
            task.setProcessed(Boolean.TRUE);
            task.setNotesProcessed("Invalid account resource");
            task = taskService.save(task);
            return;
        }

        BillingMonth billingMonth = billingMonthService.getActiveMonth();
        if (billingMonth == null) {
            task.setProcessed(Boolean.TRUE);
            task.setNotesProcessed("Invalid billing month resource. Request failed to post.");
            task = taskService.save(task);
            return;
        }
        //Payment object
        Payment payment = new Payment();
        payment.setBillingMonth(billingMonth);
        payment.setAmount(task.getAmount());
        payment.setAccount(task.getAccount());
        payment.setNotes(task.getNotes());

        DateTime transDate = payment.getTransactionDate();
        if (!billingMonthService.canTransact(transDate)) {
            task.setProcessed(Boolean.TRUE);
            task.setNotesProcessed("Invalid billing/transaction date");
            task = taskService.save(task);
            return;
        }

        // check if all values are present
        if (payment.getAmount() == null) {
            task.setProcessed(Boolean.TRUE);
            task.setNotesProcessed("Invalid amount");
            task = taskService.save(task);
            return;
        }

        if (payment.getAmount() == 0) {
            task.setProcessed(Boolean.TRUE);
            task.setNotesProcessed("Payment amount can not be zero");
            task = taskService.save(task);
            return;
        }

        if (payment.getTransactionDate() == null) {
            task.setProcessed(Boolean.TRUE);
            task.setNotesProcessed("Invalid transaction date");
            task = taskService.save(task);
            return;
        }

        DateTime today = new DateTime();
        if (payment.getTransactionDate().isAfter(today)) {
            task.setProcessed(Boolean.TRUE);
            task.setNotesProcessed("Transaction date can not be greater than now");
            task = taskService.save(task);
            return;
        }

        if (StringUtils.equalsIgnoreCase(task.getTaskType().getName(), TaskTypeConst.CREDIT_ADJUSTMENT)) {
            PaymentType paymentType = paymentTypeRepository.findByName("Credit");
            if (paymentType != null) {
                payment.setPaymentType(paymentType);
                payment.setReceiptNo("Credit Adj");

            }
        } else if (StringUtils.equalsIgnoreCase(task.getTaskType().getName(), TaskTypeConst.DEBIT_ADJUSTMENT)) {
            PaymentType paymentType = paymentTypeRepository.findByName("Debit");
            if (paymentType != null) {
                payment.setPaymentType(paymentType);
                payment.setReceiptNo("Debit Adj");
            }
        }

        if (payment.getPaymentType() == null) {
            task.setProcessed(Boolean.TRUE);
            task.setNotesProcessed("Invalid payment type");
            task = taskService.save(task);
            return;
        }

        // TODO;
        PaymentSource paymentSource = paymentSourceRepository.findByName("CASH");
        if (paymentSource != null) {
            payment.setPaymentSource(paymentSource);
        }

        if (payment.getPaymentType().isNegative()) {
            payment.setAmount(Math.abs(payment.getAmount()));
            payment.setAmount(Math.abs(payment.getAmount()) * -1);
        }
        // create resource
        payment.setAccount(account);
        Payment created = this.create(payment, accountId);
        if (created != null) {
            accountService.setUpdateBalance(account.getAccountId());
            accountService.updateBalance(accountId);

            task.setProcessed(Boolean.TRUE);
            task.setNotesProcessed("Adjustment posted");
            task = taskService.save(task);
        }

        return;

    }

    @Transactional
    public RestResponse createByAccount(RestRequestObject<Payment> requestObject, Long accountId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "payment_create");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                accountService.updateBalance(accountId);

                Account account = accountRepository.findOne(accountId);
                if (account == null) {
                    responseObject.setMessage("Invalid account");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                Payment payment = requestObject.getObject();
                if (payment.getBillingMonth() == null) {
                    responseObject.setMessage("Invalid billing month. Kindly contact your admin.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                DateTime transDate = payment.getTransactionDate();
                if (!billingMonthService.canTransact(transDate)) {
                    responseObject.setMessage("Invalid billing/transaction date");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                // check if all values are present
                if (payment.getAmount() == null) {
                    responseObject.setMessage("Invalid amount");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (payment.getAmount() == 0) {
                    responseObject.setMessage("Invalid amount");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (payment.getTransactionDate() == null) {
                    responseObject.setMessage("Invalid transaction date");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (payment.getTransactionDate().isAfter(new DateTime())) {
                    responseObject.setMessage("Transaction date can not be greater than now");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                PaymentType paymentType = paymentTypeRepository.findOne(payment.getPaymentType().getPaymentTypeId());
                if (paymentType == null) {
                    responseObject.setMessage("Invalid payment type");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                // TODO;
                PaymentSource paymentSource = paymentSourceRepository.findByName("CASH");

                if (paymentType.isUnique()) {
                    // check if payment exists
                    Payment p = paymentRepository.findByreceiptNo(payment.getReceiptNo());
                    if (p != null) {
                        responseObject.setMessage("Duplicate receipt number");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }
                }

                if (paymentType.hasComments()) {
                    response = authManager.grant(requestObject.getToken(), "payments_debit_credit");
                    if (response.getStatusCode() != HttpStatus.OK) {
                        return response;
                    }

                    if (payment.getNotes() == null) {
                        responseObject.setMessage("Notes missing");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }

                    if (payment.getNotes().isEmpty()) {
                        responseObject.setMessage("Notes missing");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }

                    if (payment.getNotes().length() <= 10) {
                        responseObject.setMessage("Notes too short");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }
                }

                if (paymentType.isNegative()) {
                    payment.setAmount(Math.abs(payment.getAmount()));
                    payment.setAmount(payment.getAmount() * -1);
                }

                if (StringUtils.equalsIgnoreCase(payment.getPaymentType().getName(), "Credit")) {
                    taskService.create(accountId, payment.getNotes(), TaskTypeConst.CREDIT_ADJUSTMENT, authManager.getEmailFromToken(requestObject.getToken()), payment.getAmount(), 0l);
                } else if (StringUtils.equalsIgnoreCase(payment.getPaymentType().getName(), "Debit")) {
                    taskService.create(accountId, payment.getNotes(), TaskTypeConst.DEBIT_ADJUSTMENT, authManager.getEmailFromToken(requestObject.getToken()), payment.getAmount(), 0l);
                } else {
                    response = authManager.grant(requestObject.getToken(), "payments_debit_credit");
                    if (response.getStatusCode() == HttpStatus.OK) {
                        responseObject.setMessage("Sorry you can not perform this action.");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }
                    // create resource
                    payment.setAccount(account);
                    payment.setPaymentType(paymentType);
                    payment.setPaymentSource(paymentSource);
                    Payment created = this.create(payment, accountId);

                    accountService.setUpdateBalance(account.getAccountId());
                    accountService.updateBalance(accountId);

                    //

                    //send sms
                    if (!created.getPaymentType().hasComments()) {
                        smsService.saveNotification(account.getAccountId(), created.getPaymentid(), 0L, SMSNotificationType.PAYMENT);
                    }

                    // package response
                    responseObject.setMessage("Payment created successfully. ");
                    responseObject.setPayload(created);
                    response = new RestResponse(responseObject, HttpStatus.CREATED);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(created.getPaymentid()));
                    auditRecord.setCurrentData(created.toString());
                    auditRecord.setParentObject("Payments");
                    auditRecord.setNotes("CREATED PAYMENT");
                    auditService.log(AuditOperation.CREATED, auditRecord);
                    //End - audit trail
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @Transactional
    public RestResponse voidReceipt(RestRequestObject<Payment> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "payment_void");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                Payment payment = paymentRepository.findOne(requestObject.getObject().getPaymentid());
                if (payment == null) {
                    responseObject.setMessage("Invalid payment resource");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                responseObject.setMessage("Not implemented");
                responseObject.setPayload("");
                response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                return response;


//                if (payment.getAmount().equals(0d)) {
//                    responseObject.setMessage("You can not void this receipt. Amount is zero.");
//                    responseObject.setPayload("");
//                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
//                    return response;
//                }
//
//                if (StringUtils.isEmpty(requestObject.getObject().getNotes())) {
//                    responseObject.setMessage("Notes can not be empty");
//                    responseObject.setPayload("");
//                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
//                    return response;
//                }

                // create resource
//                payment.setAmount(0d);
//                payment.setNotes(requestObject.getObject().getNotes());
//                Payment created = paymentRepository.save(payment);
//
//                accountService.setUpdateBalance(created.getAccount().getAccountId());


                // update balances
                //Account account = accountRepository.findOne(created.getAccount().getAccountId());

                // update account outstanding balance
                //account.setOutstandingBalance(accountService.getAccountBalance(account.getAccountId()));

                //save account info before generating notification
                //accountRepository.save(account);

                //send sms
//                if (!created.getPaymentType().hasComments()) {
//                    //smsService.saveNotification(account.getAccountId(), created.getPaymentid(), 0L, SMSNotificationType.PAYMENT);
//                }
//
//                // package response
//                responseObject.setMessage("Receipt updated successfully");
//                responseObject.setPayload(created);
//                response = new RestResponse(responseObject, HttpStatus.CREATED);
//
//                //Start - audit trail
//                AuditRecord auditRecord = new AuditRecord();
//                auditRecord.setParentID(String.valueOf(created.getPaymentid()));
//                auditRecord.setCurrentData(created.toString());
//                auditRecord.setParentObject("Payments");
//                auditRecord.setNotes("VOIDED PAYMENT");
//                auditService.log(AuditOperation.CREATED, auditRecord);
                //End - audit trail

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

    public RestResponse getAllByAccount(RestRequestObject<RestPageRequest> requestObject, Long account_id) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {

                response = authManager.grant(requestObject.getToken(), "payments_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }


                Account account = accountRepository.findOne(account_id);
                if (account == null) {
                    responseObject.setMessage("Invalid account");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                } else {

                    RestPageRequest p = requestObject.getObject();

                    Page<Payment> page;
                    page = paymentRepository.findAllByAccount(account, new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));

                    if (page.hasContent()) {

                        responseObject.setMessage("Fetched data successfully");
                        responseObject.setPayload(page);
                        response = new RestResponse(responseObject, HttpStatus.OK);
                    } else {
                        responseObject.setMessage("Your search did not match any records");
                        response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
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

    public RestResponse getAllByReceiptNo(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "payments_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest p = requestObject.getObject();
                Page<Payment> page;
                if (p.getFilter().isEmpty()) {
                    page = paymentRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                } else {
                    page = paymentRepository.findAllByReceiptNoContainsOrAccount_accNoContains(p.getFilter(), p.getFilter(), new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));

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

    public RestResponse transferPayment(RestRequestObject<Payment> requestObject, Long accountId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "payment_transfer");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Payment payment = requestObject.getObject();

                Payment p = paymentRepository.findOne(payment.getPaymentid());
                if (p == null) {
                    responseObject.setMessage("Invalid payment.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (payment.getNotes().isEmpty()) {
                    responseObject.setMessage("Please enter a reason for transferring the payment");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                p.setNotes(payment.getNotes());

                Account account = accountRepository.findOne(accountId);
                if (account == null) {
                    responseObject.setMessage("Invalid account");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (account.getAccountId() == p.getAccount().getAccountId()) {
                    responseObject.setMessage("You can not transfer payment to the same account.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (p.getPaymentType().hasComments()) {
                    responseObject.setMessage("You can not transfer this payment type");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }


                PaymentSource paymentSource = paymentSourceRepository.findByName("CASH");
                DateTime transDate = p.getTransactionDate();
                if (!billingMonthService.canTransact(transDate)) {
                    responseObject.setMessage("Invalid billing/transaction date");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                //create new billing month object
                BillingMonth billingMonth = billingMonthService.getActiveMonth();

                //transaction date


                //debit source
                Payment payment1 = new Payment();
                PaymentType paymentType = paymentTypeRepository.findOne(3L);
                payment1.setPaymentType(paymentType);
                payment1.setAmount(p.getAmount());
                payment1.setAccount(p.getAccount());
                payment1.setNotes(p.getNotes());
                payment1.setReceiptNo(p.getReceiptNo());
                payment1.setBillingMonth(billingMonth);
                payment1.setTransactionDate(transDate);

                if (payment1.getPaymentType().isNegative()) {
                    payment1.setAmount(Math.abs(p.getAmount()));
                    payment1.setAmount(p.getAmount() * -1);
                }
                payment1.setPaymentSource(paymentSource);
                paymentRepository.save(payment1);


                //credit destination
                Payment payment2 = new Payment();
                paymentType = paymentTypeRepository.findOne(2L);
                payment2.setPaymentType(paymentType);
                payment2.setAmount(p.getAmount());
                payment2.setAccount(account);
                payment2.setReceiptNo(p.getReceiptNo());
                payment2.setBillingMonth(billingMonth);
                payment2.setTransactionDate(transDate);


                if (payment2.getPaymentType().isNegative()) {
                    payment2.setAmount(Math.abs(p.getAmount()));
                    payment2.setAmount(p.getAmount() * -1);
                }
                payment2.setPaymentSource(paymentSource);
                paymentRepository.save(payment2);

                //calculate balances for both accounts
                //Account acc = accountRepository.findOne(accountId);
                //acc.setOutstandingBalance(accountService.getAccountBalance(acc.getAccountId()));
                //accountRepository.save(acc);


                //acc = accountRepository.findOne(account.getAccountId());
                //acc.setOutstandingBalance(accountService.getAccountBalance(acc.getAccountId()));
                //accountRepository.save(acc);


                //audit trail
                //Start - audit trail
//                AuditRecord auditRecord = new AuditRecord();
//                auditRecord.setParentID(String.valueOf(billId));
//                auditRecord.setParentObject("BILLS");
//                auditRecord.setCurrentData(bill.toString());
//                auditRecord.setNotes("DELETED BILL FOR:" + bill.getAccount().getAccNo());
//                auditService.log(AuditOperation.DELETED, auditRecord);
                //End - audit trail

                AccountUpdate accountUpdate = new AccountUpdate();
                accountUpdate.setAccountId(account.getAccountId());
                accountsUpdateRepository.save(accountUpdate);


                responseObject.setMessage("Payment transferred successfully.");
                responseObject.setPayload("");
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public Boolean exits(String receiptNo) {
        Boolean exists = false;
        if (paymentRepository.countByReceiptNo(receiptNo) > 0) {
            exists = Boolean.TRUE;
        }
        return exists;
    }
}
