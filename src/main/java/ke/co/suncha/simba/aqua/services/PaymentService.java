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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.aqua.models.*;
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
import org.springframework.transaction.annotation.Transactional;

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
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private SMSRepository smsRepository;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public PaymentService() {

    }

    public Double getAccountBalance(Account account) {
        // update balances

        Double balance = 0d;

        // add balance b/f
        balance += account.getBalanceBroughtForward();

        List<Bill> bills = account.getBills();
        if (bills != null) {
            for (Bill bill : bills) {
                balance += bill.getAmount();
                balance += bill.getMeterRent();

                // get bill items
                List<BillItem> billItems = bill.getBillItems();
                if (billItems != null) {
                    for (BillItem billItem : billItems) {
                        balance += billItem.getAmount();
                    }
                }
            }
        }

        // get payments
        List<Payment> payments = account.getPayments();
        if (payments != null) {
            for (Payment p : payments) {
                balance = (balance - p.getAmount());
            }
        }
        return balance;
    }

    @Transactional
    public RestResponse createByAccount(RestRequestObject<Payment> requestObject, Long accountId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "payments_create");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

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

                Calendar calendar = Calendar.getInstance();
                if (payment.getTransactionDate().after(calendar)) {
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
                // create resource
                payment.setAccount(account);
                payment.setPaymentType(paymentType);
                payment.setPaymentSource(paymentSource);
                Payment created = paymentRepository.save(payment);


                // update balances
                account = accountRepository.findOne(accountId);

                // update account outstanding balance
                account.setOutstandingBalance(this.getAccountBalance(account));

                //send sms
                try {
                    if (!account.getConsumer().getPhoneNumber().isEmpty()) {
                        SMS sms = new SMS();
                        sms.setMobileNumber(account.getConsumer().getPhoneNumber());

                        SimpleDateFormat format1 = new SimpleDateFormat("MMM dd, yyyy");
                        String today = format1.format(Calendar.getInstance().getTime());
                        //TODO; set this in config
                        String message = "Dear " + account.getConsumer().getFirstName() + ", you paid KES " + payment.getAmount() + " on " + today + ". New Water balance is KES " + account.getOutstandingBalance();
                        sms.setMessage(message);
                        smsRepository.save(sms);
                    }
                } catch (Exception ex) {

                }


                accountRepository.save(account);

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

                response = authManager.grant(requestObject.getToken(), "account_payments_list");
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

                //debit source
                Payment payment1 = new Payment();
                PaymentType paymentType = paymentTypeRepository.findOne(3L);
                payment1.setPaymentType(paymentType);
                payment1.setAmount(p.getAmount());
                payment1.setAccount(p.getAccount());
                payment1.setNotes(p.getNotes());
                payment1.setReceiptNo(p.getReceiptNo());
                payment1.setBillingMonth(p.getBillingMonth());
                payment1.setTransactionDate(p.getTransactionDate());

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
                payment2.setBillingMonth(p.getBillingMonth());
                payment2.setTransactionDate(p.getTransactionDate());


                if (payment2.getPaymentType().isNegative()) {
                    payment2.setAmount(Math.abs(p.getAmount()));
                    payment2.setAmount(p.getAmount() * -1);
                }
                payment2.setPaymentSource(paymentSource);
                paymentRepository.save(payment2);

                //calculate balances for both accounts
                Account acc = accountRepository.findOne(accountId);
                acc.setOutstandingBalance(this.getAccountBalance(acc));
                accountRepository.save(acc);

                acc = accountRepository.findOne(account.getAccountId());
                acc.setOutstandingBalance(this.getAccountBalance(acc));
                accountRepository.save(acc);


                //audit trail
                //Start - audit trail
//                AuditRecord auditRecord = new AuditRecord();
//                auditRecord.setParentID(String.valueOf(billId));
//                auditRecord.setParentObject("BILLS");
//                auditRecord.setCurrentData(bill.toString());
//                auditRecord.setNotes("DELETED BILL FOR:" + bill.getAccount().getAccNo());
//                auditService.log(AuditOperation.DELETED, auditRecord);
                //End - audit trail


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

}
