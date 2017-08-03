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
package ke.co.suncha.simba.aqua.billing;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.admin.version.ReleaseManager;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.account.BillingFrequency;
import ke.co.suncha.simba.aqua.account.OnStatus;
import ke.co.suncha.simba.aqua.account.QAccount;
import ke.co.suncha.simba.aqua.account.scheme.SchemeRepository;
import ke.co.suncha.simba.aqua.makerChecker.tasks.Task;
import ke.co.suncha.simba.aqua.makerChecker.tasks.TaskService;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.reports.AccountsReportRequest;
import ke.co.suncha.simba.aqua.reports.BillRecord;
import ke.co.suncha.simba.aqua.reports.MeterRecord;
import ke.co.suncha.simba.aqua.reports.ReportObject;
import ke.co.suncha.simba.aqua.repository.*;
import ke.co.suncha.simba.aqua.services.*;
import ke.co.suncha.simba.aqua.utils.BillMeta;
import ke.co.suncha.simba.aqua.utils.BillRequest;
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

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Service
public class BillService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BillItemRepository billItemRepository;

    @Autowired
    private SimbaOptionService optionService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BillingMonthRepository billingMonthRepository;

    @Autowired
    private TariffService tariffService;

    @Autowired
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    SMSRepository smsRepository;

    @Autowired
    AuditService auditService;

    @Autowired
    AccountService accountService;

    @Autowired
    SMSService smsService;

    @Autowired
    MPESARepository mpesaRepository;

    @Autowired
    MeterRepository meterRepository;

    @Autowired
    SchemeRepository schemeRepository;

    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    BillingMonthService billingMonthService;

    @Autowired
    private TaskService taskService;

    @Autowired
    EntityManager entityManager;


    @Autowired
    ReleaseManager releaseManager;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public BillService() {
    }

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    private BillingMonth getActiveBillingMonth() {
        BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);
        return billingMonth;
    }

    @Transactional
    public RestResponse bill(RestRequestObject<BillRequest> requestObject, Long accountId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "bill_account");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                Account account = accountRepository.findOne(accountId);
                if (account == null) {
                    responseObject.setMessage("Invalid account.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                if (!billingMonthService.canTransact(new DateTime())) {
                    responseObject.setMessage("Sorry we can not complete your request, invalid billing month/transaction date");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }


                //accountService.setUpdateBalance(accountId);
                //accountService.updateBalance(accountId);

                if (!account.isActive()) {
                    responseObject.setMessage("Sorry we can not complete your request, the account is inactive.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                if (account.getOnStatus() != OnStatus.TURNED_ON) {
                    responseObject.setMessage("Sorry we can not complete your request, you can only bill account which has been turned on.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                //Check if meter validation is enabled
                Boolean billOnlyMeteredAccounts = Boolean.parseBoolean(optionService.getOption("BILL_ONLY_METERED_ACCOUNTS").getValue());
                if (billOnlyMeteredAccounts) {
                    if (account.getMeter() == null) {
                        responseObject.setMessage("Sorry we can not complete your request, the account is not metered.");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                        return response;
                    }
                }
                //check if there is pending unallocated payments
                Boolean billOnlyWithZeroUnAllocatedPayments = Boolean.parseBoolean(optionService.getOption("BILL_WHEN_ZERO_UNALLOCATED_PAYMENTS").getValue());
                if (billOnlyWithZeroUnAllocatedPayments) {
                    Double notAllocated = 0d;
                    try {
                        notAllocated = mpesaRepository.findSumAllocated(0);
                    } catch (Exception ex) {
                    }
                }

                log.info("Billing account:" + account.getAccNo());

                Bill lastBill = this.getAccountLastBill(accountId);

                if (account.getBillingFrequency() == BillingFrequency.MONTHLY) {

                    if (lastBill.isBilled()) {
                        responseObject.setMessage("The account has already being billed this month.");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                        return response;
                    }

                }

                BillRequest billRequest = requestObject.getObject();
                Bill bill = new Bill();


                if (account.getBillingFrequency() == BillingFrequency.ANY_TIME) {
                    if (billRequest.getTransactionDate() == null) {
                        responseObject.setMessage("The account billing frequency requires you provide a transaction date.");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                        return response;
                    }

                    if (!billingMonthService.canTransact(billRequest.getTransactionDate())) {
                        responseObject.setMessage("Sorry we can not complete your request, transaction date does not fall within the current billing month");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                        return response;
                    }
                    //set transaction date
                    bill.setTransactionDate(billRequest.getTransactionDate());
                }


                //Check if not to include water sale in bill
                if (!billRequest.getBillWaterSale()) {
                    if (billRequest.getPreviousReading() != billRequest.getCurrentReading()) {
                        responseObject.setMessage("Previous reading and current reading miss match");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                        return response;
                    }
                }

                //set
                //Only allow editing of previous reading if user has a role
                if (lastBill != null) {
                    response = authManager.grant(requestObject.getToken(), "bill_edit_previous_reading");
                    if (response.getStatusCode() != HttpStatus.OK) {
                        if (!billRequest.getPreviousReading().equals(lastBill.getCurrentReading())) {
                            responseObject.setMessage("Sorry you are not authorized to edit previous reading when billing. Please contact your admin.");
                            responseObject.setPayload("");
                            response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                            return response;
                        }
                    }
                }

                //TODO; check nulls
                bill.setCurrentReading(billRequest.getCurrentReading());
                bill.setPreviousReading(billRequest.getPreviousReading());

                //get units consumed
                Double unitsConsumed = bill.getCurrentReading() - bill.getPreviousReading();

                Integer billOnAverageUnits = 0;
                try {
                    billOnAverageUnits = Integer.parseInt(optionService.getOption("BILL_ON_AVERAGE_UNITS").getValue());
                } catch (Exception ex) {

                }
                if (billOnAverageUnits == null) {
                    billOnAverageUnits = 0;
                }

                if (unitsConsumed > billOnAverageUnits) {
                    bill.setConsumptionType("Actual");
                } else {
                    if (billRequest.getBillWaterSale()) {
                        unitsConsumed = account.getAverageConsumption();
                    }
                    bill.setConsumptionType("Average");
                }

                //set units consumed
                bill.setUnitsBilled(unitsConsumed);
                bill.setBillWaterSale(billRequest.getBillWaterSale());

                //set meter rent
                if (account.isMetered()) {
                    if (account.getMeter().getMeterOwner().getCharge()) {
                        bill.setMeterRent(account.getMeter().getMeterSize().getRentAmount());
                    }

                    //
                    Meter dbMeter = meterRepository.findOne(account.getMeter().getMeterId());
                    if (dbMeter != null) {
                        dbMeter.setIsNew(Boolean.FALSE);
                        meterRepository.save(dbMeter);
                    }
                }

                //set billing amount
                BillMeta billMeta = new BillMeta();
                billMeta.setUnits(unitsConsumed);
                billMeta = tariffService.calculate(billMeta, accountId);

                bill.setAmount(billMeta.getAmount());
                bill.setContent(billMeta.getContent());

                //check billed amount
                if (billRequest.getBillWaterSale()) {
                    if (bill.getAmount() <= 0) {
                        responseObject.setMessage("Sorry we could not save the bill. Invalid billing amount.");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }
                } else {
                    bill.setAmount(0d);
                }

                //set billcode
                //String billCode =
                BillingMonth activeBillingMonth = this.getActiveBillingMonth();
                if (activeBillingMonth == null) {
                    responseObject.setMessage("Sorry we could not save the bill. Invalid billing month.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }
                bill.setBillingMonth(activeBillingMonth);
                bill.setBillCode(activeBillingMonth.getCode());


                //set average
                bill.setAverageConsumption(account.getAverageConsumption());

                //
                log.info("adding bill to data store");

                //set account
                bill.setAccount(account);

                //set parent account
                bill.setParentAccount(account);
                bill.setTransferred(Boolean.FALSE);
                Bill createdBill = billRepository.save(bill);

                log.info("Applying charges to bill...");
                if (billRequest.getBillItemTypes() != null) {
                    if (!billRequest.getBillItemTypes().isEmpty()) {
                        List<BillItem> billItems = new ArrayList<>();
                        for (BillItemType bit : billRequest.getBillItemTypes()) {
                            log.info("Bill item type:" + bit.getName());
                            BillItem bi = new BillItem();
                            bi.setAmount(bit.getAmount());
                            bi.setBill(createdBill);
                            bi.setBillItemType(bit);

                            BillItem createdBillItem = billItemRepository.save(bi);

                            //add to list
                            log.info("Adding other charges:" + bit.getName());
                            billItems.add(createdBillItem);
                        }
//                        bill.setBillItems(billItems);
                        createdBill.setBillItems(billItems);
                        //createdBill = billRepository.save(bill);
                    }
                }

                //

                Double totalAmount = 0.0;
                if (account.isMetered()) {
                    log.info("Applying meter rent...");
                    if (account.getMeter().getMeterOwner().getCharge()) {
                        totalAmount += account.getMeter().getMeterSize().getRentAmount();
                    }
                }

                log.info("Getting total billed...");
                Boolean accountIsActive = true;
                totalAmount += createdBill.getAmount();
                if (createdBill.getBillItems() != null) {
                    if (!createdBill.getBillItems().isEmpty()) {
                        for (BillItem bi : createdBill.getBillItems()) {
                            totalAmount += bi.getAmount();
                            if (!bi.getBillItemType().isActive()) {
                                accountIsActive = false;
                            }
                        }
                    }
                }
                //update total amount
                createdBill.setTotalBilled(totalAmount);

                createdBill = billRepository.save(bill);

                //save outstanding balance
                log.info("Saving outstanding balance...");
                account.setActive(accountIsActive);
                account.setUpdateBalance(Boolean.TRUE);
                account = accountRepository.save(account);

                accountService.updateBalance(account.getAccountId());

                //send sms
                if (account.getBillingFrequency() == BillingFrequency.MONTHLY) {
                    log.info("Saving SMS notification...");
                    smsService.saveNotification(account.getAccountId(), 0L, createdBill.getBillId(), SMSNotificationType.BILL);
                }

                log.info("Account billed successfully:" + account.getAccNo());

                responseObject.setMessage("Account billed successfully");
                responseObject.setPayload(bill);
                response = new RestResponse(responseObject, HttpStatus.OK);


                //Start - audit trail
                AuditRecord auditRecord = new AuditRecord();
                auditRecord.setParentID(String.valueOf(bill.getBillId()));
                auditRecord.setParentObject("BILLS");
                auditRecord.setCurrentData(bill.toString());
                auditRecord.setNotes("CREATED BILL FOR:" + bill.getAccount().getAccNo());
                auditService.log(AuditOperation.CREATED, auditRecord);
                //End - audit trail
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @Transactional
    public RestResponse getAllByAccount(RestRequestObject<RestPageRequest> requestObject, Long account_id) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_view");
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

                    Page<Bill> page;
                    page = billRepository.findAllByAccount(account, new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));

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

    @Transactional
    private Bill getAccountLastBill(Long accountId) {
        Bill lastBill = new Bill();
        // get current billing month
        BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);

        Account account = accountRepository.findOne(accountId);


        log.info("Getting the most current bill for:" + account.getAccNo());

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QBill.bill.account.accountId.eq(accountId));

        //Check account billing frequency. If account billed montly do not fetch transfered bills
        if (account.getBillingFrequency() == BillingFrequency.MONTHLY) {
            builder.and(QBill.bill.transferred.eq(Boolean.FALSE));
        }

        JPAQuery query = new JPAQuery(entityManager);
        Long lastBillId = query.from(QBill.bill).where(builder).orderBy(QBill.bill.billCode.desc()).singleResult(QBill.bill.billId.max());


        if (lastBillId == null) {
            // seems its initial bill so check if account is metered
            if (account.isMetered()) {
                lastBill.setCurrentReading(account.getMeter().getInitialReading());
                lastBill.setBilled(false);
            } else {
                // TODO;
                lastBill.setCurrentReading(0.0);
                lastBill.setBilled(false);
            }
        } else {

            lastBill = billRepository.findOne(lastBillId);

            log.info("Most current bill:" + lastBill.toString());

            if (account.getMeter() != null) {
                if (account.getMeter().getIsNew() != null) {
                    if (account.getMeter().getIsNew()) {
                        lastBill.setCurrentReading(account.getMeter().getInitialReading());
                    }
                }
            }


            //log.info("Most current bill:" + lastBill.getBillingMonth().getMonth().get(Calendar.YEAR) + "-" + lastBill.getBillingMonth().getMonth().get(Calendar.MONTH));
            log.info("Billing month:" + billingMonth.getMonth().toString("MMM, yyyy"));

            if (lastBill.getBillingMonth().getMonth().isBefore(billingMonth.getMonth())) {
                log.info("Billed:false");
                lastBill.setBilled(Boolean.FALSE);
            } else {
                log.info("Billed:Yes");
                lastBill.setBilled(Boolean.TRUE);
            }

            if (lastBill.getBillingMonth().getMonth().equals(billingMonth.getMonth())) {
                lastBill.setBilled(Boolean.TRUE);
                log.info("Billed:Yes");
            }
//            if (lastBill.isBilled()) {
//                if (lastBill.getTransferred()) {
//                    lastBill.setBilled(Boolean.FALSE);
//                }
//            }
        }

        if (account != null) {
            if (account.getBillingFrequency() == BillingFrequency.ANY_TIME) {
                lastBill.setBilled(Boolean.FALSE);
            }
        }

        return lastBill;
    }

    @Transactional
    public void delete(Long taskId) {
        Task task = taskService.getById(taskId);
        if (task == null) {
            return;
        }
        Bill bill = billRepository.findOne(task.getRecordId());
        Bill lastBill = this.getAccountLastBill(bill.getAccount().getAccountId());
        if (lastBill == null) {
            task.setProcessed(Boolean.TRUE);
            task.setNotesProcessed("Invalid bill resource");
            task = taskService.save(task);
            return;
        }

        if (bill.getBillId() != lastBill.getBillId()) {
            task.setProcessed(Boolean.TRUE);
            task.setNotesProcessed("Sorry we could not complete your request. You can only delete the most current bill.");
            task = taskService.save(task);
            return;
        }

        DateTime transDate = new DateTime();
        transDate = transDate.withMillis(lastBill.getTransactionDate().getMillis());
        if (!billingMonthService.canTransact(transDate)) {
            task.setProcessed(Boolean.TRUE);
            task.setNotesProcessed("Sorry we could not complete your request. Invalid bill transaction date");
            task = taskService.save(task);
            return;
        }

        //region audit trail
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setParentID(String.valueOf(bill.getBillId()));
        auditRecord.setParentObject("BILLS");
        auditRecord.setCurrentData(bill.toString());
        auditRecord.setNotes("DELETE BILL FOR BILL:" + bill.getAccount().getAccNo());
        auditService.log(AuditOperation.ACCESSED, auditRecord);
        //endregion

        //delete bill
        billRepository.delete(bill.getBillId());

        //save outstanding balance
        Account account = accountRepository.findOne(task.getAccount().getAccountId());
        account.setOutstandingBalance(accountService.getAccountBalance(account.getAccountId()));
        accountRepository.save(account);

        task.setPosted(Boolean.TRUE);
        task.setProcessed(Boolean.TRUE);
        task.setNotesProcessed("Bill deleted successfully");
        taskService.save(task);
    }

    public RestResponse deleteBill(RestRequestObject<Task> requestObject, Long billId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "bill_delete");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                String emailAddress = authManager.getEmailFromToken(requestObject.getToken());
                Bill bill = billRepository.findOne(billId);
                Long accountId = bill.getAccount().getAccountId();
                if (bill == null) {
                    responseObject.setMessage("Invalid bill.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                Bill lastBill = this.getAccountLastBill(bill.getAccount().getAccountId());
                if (lastBill == null) {
                    responseObject.setMessage("Invalid bill.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (bill.getBillId() != lastBill.getBillId()) {
                    responseObject.setMessage("Sorry we could not complete your request. You can only delete the most current bill.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                DateTime transDate = new DateTime();
                transDate = transDate.withMillis(lastBill.getTransactionDate().getMillis());
                if (!billingMonthService.canTransact(transDate)) {
                    responseObject.setMessage("Sorry we could not complete your request. Invalid bill transaction date");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (StringUtils.isEmpty(requestObject.getObject().getNotes())) {
                    responseObject.setMessage("Sorry we could not complete you request. Notes can not be empty");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                //audit trail
                //Start - audit trail
                AuditRecord auditRecord = new AuditRecord();
                auditRecord.setParentID(String.valueOf(billId));
                auditRecord.setParentObject("BILLS");
                auditRecord.setCurrentData(bill.toString());
                auditRecord.setNotes("SUBMITED DELETE REQUEST FOR BILL:" + bill.getAccount().getAccNo());
                auditService.log(AuditOperation.ACCESSED, auditRecord);
                //End - audit trail

                //Submit request
                if (!taskService.canAdd(accountId, "DELETE_BILL")) {
                    responseObject.setMessage("Sorry we could not complete your request. You already have a pending request for this account");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                taskService.create(accountId, requestObject.getObject().getNotes(), "DELETE_BILL", emailAddress, bill.getAmount(), billId);

                //delete bill
                //billRepository.delete(bill.getBillId());

                //save outstanding balance
                //Account account = accountRepository.findOne(accountId);
                //account.setOutstandingBalance(accountService.getAccountBalance(account.getAccountId()));
                //accountRepository.save(account);

                responseObject.setMessage("Bill delete request submitted successfully.");
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

    @Transactional
    public RestResponse getLastBill(RestRequestObject<RestPageRequest> requestObject, Long accountId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "bill_account");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Bill bill = this.getAccountLastBill(accountId);
                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(bill);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @Transactional
    public Double getAccountBillsByDate(Long accountId, DateTime toDate) {
        Double amount = 0d;
        Double dbAmount = billRepository.getTotalBilledByDate(accountId, toDate.toString("yyyy-MM-dd"));
        if (dbAmount != null) {
            amount = dbAmount;
        }
        return amount;
    }

    @Transactional
    public Double getAccountBillsByTransactionDate(Long accountId, DateTime toDate) {
        Double amount = 0d;
        Double dbAmount = billRepository.getTotalBilledByTransactionDate(accountId, toDate.toString("yyyy-MM-dd"));
        if (dbAmount != null) {
            amount = dbAmount;
        }
        return amount;
    }

    @Transactional
    public Double getAccountBillsByTransDateWithBalanceBF(Long accountId, DateTime toDate) {
        Double amount = 0d;
        //Get balance brought forward
        BooleanBuilder whereAccount = new BooleanBuilder();
        whereAccount.and(QAccount.account.accountId.eq(accountId));
        JPAQuery query = new JPAQuery(entityManager);
        Double balanceBf = query.from(QAccount.account).where(whereAccount).singleResult(QAccount.account.balanceBroughtForward);
        if (balanceBf != null) {
            amount += balanceBf;
        }

        BooleanBuilder whereBills = new BooleanBuilder();
        whereBills.and(QBill.bill.account.accountId.eq(accountId));
        whereBills.and(QBill.bill.transactionDate.loe(toDate));
        query = new JPAQuery(entityManager);
        List<Tuple> tuples = query.from(QBill.bill).where(whereBills).list(QBill.bill.billId, QBill.bill.amount, QBill.bill.meterRent);
        if (tuples != null) {
            for (Tuple tuple : tuples) {
                Long billId = tuple.get(QBill.bill.billId);
                Double billAmount = tuple.get(QBill.bill.amount);
                Double meterRent = tuple.get(QBill.bill.meterRent);
                amount += billAmount;
                amount += meterRent;

                //get bill items
                BooleanBuilder whereBillItems = new BooleanBuilder();
                whereBillItems.and(QBillItem.billItem.bill.billId.eq(billId));
                JPAQuery queryBillItems = new JPAQuery(entityManager);
                List<Double> billItems = queryBillItems.from(QBillItem.billItem).where(whereBillItems).list(QBillItem.billItem.amount);
                if (billItems != null) {
                    for (Double a : billItems) {
                        amount += a;
                    }
                }
            }
        }
        return amount;
    }

    //TODO; refactor
    @Transactional
    public Double getAccountBillsByDateWithBalanceBF(Long accountId, DateTime toDate) {
        Double amount = 0d;
        //Get balance brought forward
        BooleanBuilder whereAccount = new BooleanBuilder();
        whereAccount.and(QAccount.account.accountId.eq(accountId));
        JPAQuery query = new JPAQuery(entityManager);
        Double balanceBf = query.from(QAccount.account).where(whereAccount).singleResult(QAccount.account.balanceBroughtForward);
        if (balanceBf != null) {
            amount += balanceBf;
        }

        BooleanBuilder whereBills = new BooleanBuilder();
        whereBills.and(QBill.bill.account.accountId.eq(accountId));
        whereBills.and(QBill.bill.billingMonth.month.loe(toDate));
        query = new JPAQuery(entityManager);
        List<Tuple> tuples = query.from(QBill.bill).where(whereBills).list(QBill.bill.billId, QBill.bill.amount, QBill.bill.meterRent);
        if (tuples != null) {
            for (Tuple tuple : tuples) {
                Long billId = tuple.get(QBill.bill.billId);
                Double billAmount = tuple.get(QBill.bill.amount);
                Double meterRent = tuple.get(QBill.bill.meterRent);
                amount += billAmount;
                amount += meterRent;

                //get bill items
                BooleanBuilder whereBillItems = new BooleanBuilder();
                whereBillItems.and(QBillItem.billItem.bill.billId.eq(billId));
                JPAQuery queryBillItems = new JPAQuery(entityManager);
                List<Double> billItems = queryBillItems.from(QBillItem.billItem).where(whereBillItems).list(QBillItem.billItem.amount);
                if (billItems != null) {
                    for (Double a : billItems) {
                        amount += a;
                    }
                }
            }
        }
        return amount;
    }

    @Transactional
    public RestResponse getMeterReadingsReport(RestRequestObject<AccountsReportRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_meter_readings");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                AccountsReportRequest accountsReportRequest = requestObject.getObject();
                if (accountsReportRequest.getBillingMonthId() == null) {
                    responseObject.setMessage("Please select billing month.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                BillingMonth billingMonth = billingMonthRepository.findOne(accountsReportRequest.getBillingMonthId());
                if (billingMonth == null) {
                    responseObject.setMessage("Invalid billing month");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                BooleanBuilder builder = new BooleanBuilder();
                builder.and(QBill.bill.billingMonth.billingMonthId.eq(accountsReportRequest.getBillingMonthId()));

                //On cut off
                if (accountsReportRequest.getIsCutOff() != null) {
                    Boolean isActive = Boolean.TRUE;
                    if (accountsReportRequest.getIsCutOff()) {
                        isActive = Boolean.FALSE;
                    }
                    builder.and(QBill.bill.account.active.eq(isActive));
                }

                //Zone
                if (accountsReportRequest.getZoneId() != null) {
                    builder.and(QBill.bill.account.zone.zoneId.eq(accountsReportRequest.getZoneId()));
                } else {
                    //Scheme
                    if (accountsReportRequest.getSchemeId() != null) {
                        List<BigInteger> zoneIDs = zoneRepository.findAllBySchemeId(accountsReportRequest.getSchemeId());
                        if (!zoneIDs.isEmpty()) {
                            BooleanBuilder zoneBuilder = new BooleanBuilder();
                            for (BigInteger zoneID : zoneIDs) {
                                zoneBuilder.or(QBill.bill.account.zone.zoneId.eq(zoneID.longValue()));
                            }
                            builder.and(zoneBuilder);
                        }
                    }
                }

                Iterable<Bill> bills = billRepository.findAll(builder);
                List<MeterRecord> meterRecords = new ArrayList<>();
                Double totalUnits = 0d;
                for (Bill b : bills) {
                    MeterRecord mr = new MeterRecord();
                    if (b.getAccount() != null) {
                        mr.setAccName(b.getAccount().getAccName());
                        mr.setAccNo(b.getAccount().getAccNo());
                        mr.setZone(b.getAccount().getZone().getName());
                    }
                    mr.setUnits(b.getUnitsBilled());
                    mr.setAverage(b.getAverageConsumption());
                    mr.setConsumption(b.getConsumptionType());
                    mr.setCurrentReading(b.getCurrentReading());
                    mr.setPreviousReading(b.getPreviousReading());
                    totalUnits += mr.getUnits();
                    meterRecords.add(mr);
                }
                log.info("Generating meter reading report...");

                //send report
                ReportObject report = new ReportObject();
                report.setAmount(Double.valueOf(totalUnits.toString()));
                report.setDate(Calendar.getInstance());


                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                report.setTitle(this.optionService.getOption("REPORT:METER_READINGS").getValue());
                report.setContent(meterRecords);

                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(report);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }

        return response;
    }

    public RestResponse getMeterStopsReport(RestRequestObject<AccountsReportRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_meter_stops");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                AccountsReportRequest accountsReportRequest = requestObject.getObject();
                if (accountsReportRequest.getBillingMonthId() == null) {
                    responseObject.setMessage("Please select billing month.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                BillingMonth billingMonth = billingMonthRepository.findOne(accountsReportRequest.getBillingMonthId());
                if (billingMonth == null) {
                    responseObject.setMessage("Invalid billing month");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                BooleanBuilder builder = new BooleanBuilder();
                builder.and(QBill.bill.billingMonth.billingMonthId.eq(accountsReportRequest.getBillingMonthId()));
                builder.and(QBill.bill.currentReading.subtract(QBill.bill.previousReading).eq(0d));

                //On cut off
                if (accountsReportRequest.getIsCutOff() != null) {
                    Boolean isActive = Boolean.TRUE;
                    if (accountsReportRequest.getIsCutOff()) {
                        isActive = Boolean.FALSE;
                    }
                    builder.and(QBill.bill.account.active.eq(isActive));
                }

                //Zone
                if (accountsReportRequest.getZoneId() != null) {
                    builder.and(QBill.bill.account.zone.zoneId.eq(accountsReportRequest.getZoneId()));
                } else {
                    //Scheme
                    if (accountsReportRequest.getSchemeId() != null) {
                        List<BigInteger> zoneIDs = zoneRepository.findAllBySchemeId(accountsReportRequest.getSchemeId());
                        if (!zoneIDs.isEmpty()) {
                            BooleanBuilder zoneBuilder = new BooleanBuilder();
                            for (BigInteger zoneID : zoneIDs) {
                                zoneBuilder.or(QBill.bill.account.zone.zoneId.eq(zoneID.longValue()));
                            }
                            builder.and(zoneBuilder);
                        }
                    }
                }
                Iterable<Bill> bills = billRepository.findAll(builder);

                List<MeterRecord> meterRecords = new ArrayList<>();

                Double totalUnits = 0.0;
                for (Bill b : bills) {
                    MeterRecord mr = new MeterRecord();
                    if (b.getAccount() != null) {
                        mr.setAccName(b.getAccount().getAccName());
                        mr.setAccNo(b.getAccount().getAccNo());
                        mr.setZone(b.getAccount().getZone().getName());
                    }
                    mr.setUnits(b.getUnitsBilled());
                    mr.setAverage(b.getAverageConsumption());
                    mr.setConsumption(b.getConsumptionType());
                    mr.setCurrentReading(b.getCurrentReading());
                    mr.setPreviousReading(b.getPreviousReading());
                    totalUnits += mr.getUnits();
                    meterRecords.add(mr);
                }

                //send report
                ReportObject report = new ReportObject();
                report.setAmount(Double.valueOf(totalUnits.toString()));
                report.setDate(Calendar.getInstance());

                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                report.setTitle(this.optionService.getOption("REPORT:METER_STOPS").getValue());
                report.setContent(meterRecords);

                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(report);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getNegativeReadingsReport(RestRequestObject<AccountsReportRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_negative_readings");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                AccountsReportRequest accountsReportRequest = requestObject.getObject();
                if (accountsReportRequest.getBillingMonthId() == null) {
                    responseObject.setMessage("Please select billing month.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                BillingMonth billingMonth = billingMonthRepository.findOne(accountsReportRequest.getBillingMonthId());
                if (billingMonth == null) {
                    responseObject.setMessage("Invalid billing month");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                BooleanBuilder builder = new BooleanBuilder();
                builder.and(QBill.bill.billingMonth.billingMonthId.eq(accountsReportRequest.getBillingMonthId()));
                builder.and(QBill.bill.currentReading.subtract(QBill.bill.previousReading).lt(0));

                //On cut off
                if (accountsReportRequest.getIsCutOff() != null) {
                    Boolean isActive = Boolean.TRUE;
                    if (accountsReportRequest.getIsCutOff()) {
                        isActive = Boolean.FALSE;
                    }
                    builder.and(QBill.bill.account.active.eq(isActive));
                }

                //Zone
                if (accountsReportRequest.getZoneId() != null) {
                    builder.and(QBill.bill.account.zone.zoneId.eq(accountsReportRequest.getZoneId()));
                } else {
                    //Scheme
                    if (accountsReportRequest.getSchemeId() != null) {
                        List<BigInteger> zoneIDs = zoneRepository.findAllBySchemeId(accountsReportRequest.getSchemeId());
                        if (!zoneIDs.isEmpty()) {
                            BooleanBuilder zoneBuilder = new BooleanBuilder();
                            for (BigInteger zoneID : zoneIDs) {
                                zoneBuilder.or(QBill.bill.account.zone.zoneId.eq(zoneID.longValue()));
                            }
                            builder.and(zoneBuilder);
                        }
                    }
                }

                Iterable<Bill> bills = billRepository.findAll(builder);
                List<MeterRecord> meterRecords = new ArrayList<>();
                Double totalUnits = 0d;
                for (Bill b : bills) {
                    MeterRecord mr = new MeterRecord();
                    if (b.getAccount() != null) {
                        mr.setAccName(b.getAccount().getAccName());
                        mr.setAccNo(b.getAccount().getAccNo());
                        mr.setZone(b.getAccount().getZone().getName());
                    }
                    mr.setUnits(b.getUnitsBilled());
                    mr.setAverage(b.getAverageConsumption());
                    mr.setConsumption(b.getConsumptionType());
                    mr.setCurrentReading(b.getCurrentReading());
                    mr.setPreviousReading(b.getPreviousReading());
                    totalUnits += mr.getUnits();
                    meterRecords.add(mr);
                }

                //send report
                ReportObject report = new ReportObject();
                report.setAmount(Double.valueOf(totalUnits.toString()));
                report.setDate(Calendar.getInstance());

                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                report.setTitle(this.optionService.getOption("REPORT:NEGATIVE_METER_READINGS").getValue());
                report.setContent(meterRecords);

                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(report);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getBilledAmountReport(RestRequestObject<AccountsReportRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_billed_amount");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                AccountsReportRequest accountsReportRequest = requestObject.getObject();
                if (accountsReportRequest.getBillingMonthId() == null) {
                    responseObject.setMessage("Please select billing month.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                BillingMonth billingMonth = billingMonthRepository.findOne(accountsReportRequest.getBillingMonthId());
                if (billingMonth == null) {
                    responseObject.setMessage("Invalid billing month");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                BooleanBuilder builder = new BooleanBuilder();
                builder.and(QBill.bill.billingMonth.billingMonthId.eq(accountsReportRequest.getBillingMonthId()));

                //On cut off
                if (accountsReportRequest.getIsCutOff() != null) {
                    Boolean isActive = Boolean.TRUE;
                    if (accountsReportRequest.getIsCutOff()) {
                        isActive = Boolean.FALSE;
                    }
                    builder.and(QBill.bill.account.active.eq(isActive));
                }

                //Zone
                if (accountsReportRequest.getZoneId() != null) {
                    builder.and(QBill.bill.account.zone.zoneId.eq(accountsReportRequest.getZoneId()));
                } else {
                    //Scheme
                    if (accountsReportRequest.getSchemeId() != null) {
                        List<BigInteger> zoneIDs = zoneRepository.findAllBySchemeId(accountsReportRequest.getSchemeId());
                        if (!zoneIDs.isEmpty()) {
                            BooleanBuilder zoneBuilder = new BooleanBuilder();
                            for (BigInteger zoneID : zoneIDs) {
                                zoneBuilder.or(QBill.bill.account.zone.zoneId.eq(zoneID.longValue()));
                            }
                            builder.and(zoneBuilder);
                        }
                    }
                }

                Iterable<Bill> bills = billRepository.findAll(builder);
                List<BillRecord> billRecords = new ArrayList<>();

                Double totalUnits = 0.0;
                Double totalAmountBilled = 0.0;
                Double totalMeterRent = 0.0;
                Double totalCharges = 0.0;

                for (Bill b : bills) {
                    BillRecord br = new BillRecord();
                    if (b.getAccount() != null) {
                        br.setAccName(b.getAccount().getAccName());
                        br.setAccNo(b.getAccount().getAccNo());
                        br.setZone(b.getAccount().getZone().getName());
                    }
                    br.setUnits(b.getUnitsBilled());
                    br.setAverage(b.getAverageConsumption());
                    br.setConsumption(b.getConsumptionType());
                    br.setCurrentReading(b.getCurrentReading());
                    br.setPreviousReading(b.getPreviousReading());
                    br.setAmountBilled(b.getAmount());
                    br.setMeterRent(b.getMeterRent());

                    //other charges
                    Double charges = 0.0;
                    if (!b.getBillItems().isEmpty()) {
                        for (BillItem bi : b.getBillItems()) {
                            charges += bi.getAmount();
                        }
                    }
                    br.setOtherCharges(charges);
                    totalUnits += br.getUnits();
                    totalAmountBilled += br.getAmountBilled();
                    totalMeterRent += br.getMeterRent();
                    totalCharges += br.getOtherCharges();
                    billRecords.add(br);

                }

                //send report
                ReportObject report = new ReportObject();
                report.setAmount(totalAmountBilled);
                report.setMeterRent(totalMeterRent);
                report.setUnits(totalUnits);
                report.setCharges(totalCharges);
                report.setDate(Calendar.getInstance());

                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                report.setTitle(this.optionService.getOption("REPORT:BILLED_AMOUNT").getValue());
                report.setContent(billRecords);

                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(report);
                response = new RestResponse(responseObject, HttpStatus.OK);

            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getBillingChecklistReport(RestRequestObject<AccountsReportRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_billing_checklist");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                AccountsReportRequest accountsReportRequest = requestObject.getObject();
                if (accountsReportRequest.getBillingMonthId() == null) {
                    responseObject.setMessage("Please select billing month.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                BillingMonth billingMonth = billingMonthRepository.findOne(accountsReportRequest.getBillingMonthId());
                if (billingMonth == null) {
                    responseObject.setMessage("Invalid billing month");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                BooleanBuilder builder = new BooleanBuilder();
                builder.and(QBill.bill.billingMonth.billingMonthId.eq(accountsReportRequest.getBillingMonthId()));

                //On cut off
                if (accountsReportRequest.getIsCutOff() != null) {
                    Boolean isActive = Boolean.TRUE;
                    if (accountsReportRequest.getIsCutOff()) {
                        isActive = Boolean.FALSE;
                    }
                    builder.and(QBill.bill.account.active.eq(isActive));
                }

                //Zone
                if (accountsReportRequest.getZoneId() != null) {
                    builder.and(QBill.bill.account.zone.zoneId.eq(accountsReportRequest.getZoneId()));
                } else {
                    //Scheme
                    if (accountsReportRequest.getSchemeId() != null) {
                        List<BigInteger> zoneIDs = zoneRepository.findAllBySchemeId(accountsReportRequest.getSchemeId());
                        if (!zoneIDs.isEmpty()) {
                            BooleanBuilder zoneBuilder = new BooleanBuilder();
                            for (BigInteger zoneID : zoneIDs) {
                                zoneBuilder.or(QBill.bill.account.zone.zoneId.eq(zoneID.longValue()));
                            }
                            builder.and(zoneBuilder);
                        }
                    }
                }

                Iterable<Bill> bills = billRepository.findAll(builder);
                List<BillRecord> billRecords = new ArrayList<>();

                Double totalUnits = 0.0;
                Double totalAmountBilled = 0.0;
                Double totalMeterRent = 0.0;
                Double totalCharges = 0.0;

                for (Bill b : bills) {
                    BillRecord br = new BillRecord();
                    if (b.getAccount() != null) {
                        br.setAccName(b.getAccount().getAccName());
                        br.setAccNo(b.getAccount().getAccNo());
                        br.setZone(b.getAccount().getZone().getName());
                    }
                    br.setUnits(b.getUnitsBilled());
                    br.setAverage(b.getAverageConsumption());
                    br.setConsumption(b.getConsumptionType());
                    br.setCurrentReading(b.getCurrentReading());
                    br.setPreviousReading(b.getPreviousReading());
                    br.setAmountBilled(b.getAmount());
                    br.setMeterRent(b.getMeterRent());

                    //other charges
                    Double charges = 0.0;
                    if (!b.getBillItems().isEmpty()) {
                        for (BillItem bi : b.getBillItems()) {
                            charges += bi.getAmount();
                        }
                    }
                    br.setOtherCharges(charges);
                    totalUnits += br.getUnits();
                    totalAmountBilled += br.getAmountBilled();
                    totalMeterRent += br.getMeterRent();
                    totalCharges += br.getOtherCharges();
                    billRecords.add(br);

                }

                //send report
                ReportObject report = new ReportObject();
                report.setAmount(totalAmountBilled);
                report.setMeterRent(totalMeterRent);
                report.setUnits(totalUnits);
                report.setCharges(totalCharges);
                report.setDate(Calendar.getInstance());

                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                report.setTitle(this.optionService.getOption("REPORT:BILLING_CHECKLIST").getValue());
                report.setContent(billRecords);

                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(report);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getBillOnAverageUnits() {
        try {
            Integer units = 0;
            try {
                String dbUnits = optionService.getOption("BILL_ON_AVERAGE_UNITS").getValue();
                if (StringUtils.isNumeric(dbUnits)) {
                    units = Integer.valueOf(dbUnits);
                }
            } catch (Exception ex) {

            }
            responseObject.setPayload(units);
            responseObject.setMessage("");
            response = new RestResponse(responseObject, HttpStatus.OK);
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public Bill getById(Long billId) {
        return billRepository.findOne(billId);
    }

    @PostConstruct
    public void addPermissions() {
        releaseManager.addPermission("bill_transfer");
    }

    public RestResponse transferBill(RestRequestObject<Account> requestObject, Long billId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "bill_transfer");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                String emailAddress = authManager.getEmailFromToken(requestObject.getToken());
                Bill bill = billRepository.findOne(billId);
                Long parentAccountId = bill.getAccount().getAccountId();

                if (bill == null) {
                    responseObject.setMessage("Invalid bill resource.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                Account parentAccount = accountService.getByAccountId(parentAccountId);

                if (parentAccount.getBillingFrequency() != BillingFrequency.ANY_TIME) {
                    responseObject.setMessage("Invalid account billing frequency.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                Account account = accountService.getByAccountId(requestObject.getObject().getAccountId());
                if (account == null) {
                    responseObject.setMessage("Invalid account resource");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (account.getBillingFrequency() != BillingFrequency.MONTHLY) {
                    responseObject.setMessage("Invalid account billing frequency.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (account.getAccountId() == bill.getAccount().getAccountId()) {
                    responseObject.setMessage("You can not perform bill to same account.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }


                DateTime transDate = new DateTime();
                transDate = transDate.withMillis(bill.getTransactionDate().getMillis());
                if (!billingMonthService.canTransact(transDate)) {
                    responseObject.setMessage("Sorry we could not complete your request. Invalid bill transaction date");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }


                //audit trail
                //Start - audit trail
                AuditRecord auditRecord = new AuditRecord();
                auditRecord.setParentID(String.valueOf(billId));
                auditRecord.setParentObject("BILLS");
                auditRecord.setCurrentData(bill.toString());
                auditRecord.setNotes("SUBMITTED TRANSFER BILL FROM:" + bill.getAccount().getAccNo() + " TO: " + account.getAccNo());
                auditService.log(AuditOperation.ACCESSED, auditRecord);
                //End - audit trail


                //set transfer params
                bill.setTransferred(Boolean.TRUE);
                bill.setAccount(account);
                bill.setLastModifiedDate(new DateTime());
                bill = billRepository.save(bill);

                //Update the two account balances
                accountService.updateBalance(bill.getParentAccount().getAccountId());
                accountService.updateBalance(bill.getAccount().getAccountId());

                responseObject.setMessage("Bill transfer request submitted successfully.");
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
