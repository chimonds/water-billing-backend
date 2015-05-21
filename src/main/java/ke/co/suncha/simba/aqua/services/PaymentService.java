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
import ke.co.suncha.simba.aqua.models.Account;
import ke.co.suncha.simba.aqua.models.Bill;
import ke.co.suncha.simba.aqua.models.BillItem;
import ke.co.suncha.simba.aqua.models.Payment;
import ke.co.suncha.simba.aqua.models.PaymentSource;
import ke.co.suncha.simba.aqua.models.PaymentType;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.repository.PaymentRepository;
import ke.co.suncha.simba.aqua.repository.PaymentSourceRepository;
import ke.co.suncha.simba.aqua.repository.PaymentTypeRepository;

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
                balance -= p.getAmount();
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
                // check if all values are present
                if (payment.getAmount() == null) {
                    responseObject.setMessage("Invalid amount");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                if (payment.getAmount() == 0) {
                    responseObject.setMessage("Invalid amount");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                if (payment.getTransactionDate() == null) {
                    responseObject.setMessage("Invalid transaction date");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                Calendar calendar = Calendar.getInstance();
                if (payment.getTransactionDate().after(calendar)) {
                    responseObject.setMessage("Transaction date can not be greater than now");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                PaymentType paymentType = paymentTypeRepository.findOne(payment.getPaymentType().getPaymentTypeId());
                if (paymentType == null) {
                    responseObject.setMessage("Invalid payment type");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
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
                        response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                        return response;
                    }
                }

                if (paymentType.hasComments()) {
                    if (payment.getNotes() == null) {
                        responseObject.setMessage("Notes missing");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                        return response;
                    }

                    if (payment.getNotes().isEmpty()) {
                        responseObject.setMessage("Notes missing");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                        return response;
                    }

                    if (payment.getNotes().length() <= 10) {
                        responseObject.setMessage("Notes too short");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                        return response;
                    }
                }

                if (paymentType.isNegative()) {
                    payment.setAmount(payment.getAmount() * -1);
                }
                // create resource
                payment.setAccount(account);
                payment.setPaymentType(paymentType);
                payment.setPaymentSource(paymentSource);
                Payment created = paymentRepository.save(payment);

                // update balances
                account = accountRepository.findOne(accountId);
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
                        balance -= p.getAmount();
                    }
                }
                // update account outstanding balance
                account.setOutstandingBalance(balance);


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
                    page = paymentRepository.findAllByReceiptNoContains(p.getFilter(), new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));

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

}
