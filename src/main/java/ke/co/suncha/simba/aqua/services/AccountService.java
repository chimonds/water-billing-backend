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

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ke.co.suncha.simba.aqua.reports.AccountRecord;
import ke.co.suncha.simba.aqua.reports.BalancesReport;
import ke.co.suncha.simba.aqua.reports.ReportObject;
import ke.co.suncha.simba.aqua.reports.ReportsParam;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.repository.ConsumerRepository;

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
import scala.util.parsing.json.JSON;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Service
@Scope("prototype")
public class AccountService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ConsumerRepository consumerRepository;

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

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public AccountService() {

    }

    @Transactional
    public RestResponse create(RestRequestObject<Account> requestObject, Long id) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_create");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Account account = requestObject.getObject();
                Account acc = accountRepository.findByaccNo(account.getAccNo());

                Consumer consumer = consumerRepository.findOne(id);

                if (acc != null) {
                    responseObject.setMessage("Account already exists");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else if (consumer == null) {
                    responseObject.setMessage("Invalid consumer");

                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else {
                    // create resource
                    acc = new Account();

                    acc.setAccNo(account.getAccNo());
                    acc.setAverageConsumption(account.getAverageConsumption());
                    acc.setBalanceBroughtForward(account.getBalanceBroughtForward());
                    Account created = accountRepository.save(acc);

                    created.setLocation(account.getLocation());
                    created.setZone(account.getZone());
                    created.setTariff(account.getTariff());
                    created.setConsumer(consumer);
                    accountRepository.save(created);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(created.getAccountId()));
                    auditRecord.setParentObject("Account");
                    auditRecord.setCurrentData(created.toString());
                    auditRecord.setNotes("CREATED ACCOUNT");
                    auditService.log(AuditOperation.CREATED, auditRecord);
                    //End - audit trail

                    // package response
                    responseObject.setMessage("Account created successfully. ");
                    responseObject.setPayload(created);
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

    @Transactional
    public RestResponse update(RestRequestObject<Account> requestObject, Long id) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Account account = requestObject.getObject();
                Account acc = accountRepository.findOne(id);

                if (acc == null) {
                    responseObject.setMessage("Account not found");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else {
                    // setup resource
                    // TODO;
                    acc.setLocation(account.getLocation());
                    acc.setZone(account.getZone());
                    acc.setTariff(account.getTariff());

                    acc.setAccNo(account.getAccNo());
                    acc.setAverageConsumption(account.getAverageConsumption());
                    acc.setBalanceBroughtForward(account.getBalanceBroughtForward());

                    // save
                    accountRepository.save(acc);
                    responseObject.setMessage("Account  updated successfully");
                    responseObject.setPayload(acc);
                    response = new RestResponse(responseObject, HttpStatus.OK);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(acc.getAccountId()));
                    auditRecord.setParentObject("Account");
                    auditRecord.setCurrentData(acc.toString());
                    auditRecord.setPreviousData(account.toString());
                    auditRecord.setNotes("UPDATED ACCOUNT");
                    auditService.log(AuditOperation.UPDATED, auditRecord);
                    //End - audit trail
                }
            }
        } catch (org.hibernate.exception.ConstraintViolationException ex) {
            responseObject.setMessage("Duplicate account no");
            responseObject.setPayload("");
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            responseObject.setPayload("");
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @Transactional
    public RestResponse transfer(RestRequestObject<Account> requestObject, Long id) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_transfer");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Account account = requestObject.getObject();

                Account acc = accountRepository.findOne(account.getAccountId());
                Consumer consumer = consumerRepository.findOne(id);
                if (acc == null) {
                    responseObject.setMessage("Invalid account");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else if (consumer == null) {
                    responseObject.setMessage("Invalid consumer");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else {
                    // set new consumer
                    acc.setConsumer(consumer);

                    // save
                    accountRepository.save(acc);
                    responseObject.setMessage("Account  updated successfully");
                    responseObject.setPayload(acc);
                    response = new RestResponse(responseObject, HttpStatus.OK);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(acc.getAccountId()));
                    auditRecord.setParentObject("Account");
                    auditRecord.setCurrentData(String.valueOf(acc.getConsumer().getConsumerId()));
                    auditRecord.setPreviousData(String.valueOf(account.getConsumer().getConsumerId()));
                    auditRecord.setNotes("TRANSFERRED ACCOUNT");
                    auditService.log(AuditOperation.UPDATED, auditRecord);
                    //End - audit trail
                }
            }
        } catch (org.hibernate.exception.ConstraintViolationException ex) {
            responseObject.setMessage("Duplicate account no");
            responseObject.setPayload("");
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            responseObject.setPayload("");
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "accounts_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                RestPageRequest p = requestObject.getObject();

                Page<Account> page;
                if (p.getFilter().isEmpty()) {
                    page = accountRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                } else {
                    page = accountRepository.findByAccNoLike(p.getFilter(), new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
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

    public RestResponse getAllByConsumer(RestRequestObject<RestPageRequest> requestObject, Long consumerId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                Consumer consumer = consumerRepository.findOne(consumerId);
                if (consumer == null) {
                    responseObject.setMessage("Invalid consumer info");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else {
                    List<Account> accounts = consumer.getAccounts();
                    if (accounts.size() > 0) {
                        responseObject.setMessage("Fetched data successfully");
                        responseObject.setPayload(accounts);
                        response = new RestResponse(responseObject, HttpStatus.OK);
                    } else {
                        responseObject.setMessage("Your search did not match any records");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                    }
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return response;
    }

    public RestResponse getById(RestRequestObject<RestPageRequest> requestObject, Long id) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_view_profile");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Account account = accountRepository.findOne(id);
                if (account == null) {
                    responseObject.setMessage("Invalid account number");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(account);
                    response = new RestResponse(responseObject, HttpStatus.OK);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(account.getAccountId()));
                    auditRecord.setParentObject("Account");
                    auditRecord.setNotes("ACCOUNT PROFILE VIEW");
                    auditService.log(AuditOperation.VIEWED, auditRecord);
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

    public RestResponse getOne(RestRequestObject<Account> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_view_profile");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Account acc = requestObject.getObject();
                Account account = accountRepository.findByaccNo(acc.getAccNo());

                if (account == null) {
                    responseObject.setMessage("Invalid account number");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(account.getAccountId()));
                    auditRecord.setParentObject("Account");
                    auditRecord.setNotes("ACCOUNT PROFILE VIEW");
                    auditService.log(AuditOperation.VIEWED, auditRecord);
                    //End - audit trail

                } else {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(account);
                    response = new RestResponse(responseObject, HttpStatus.OK);
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public Double getAccountBalanceByDate(Account account, Calendar calendar) {
        // update balances
        Double balance = 0d;

        // add balance b/f
        balance += account.getBalanceBroughtForward();

        List<Bill> bills = account.getBills();
        if (bills != null) {
            for (Bill bill : bills) {
                if (bill.getTransactionDate().before(calendar)) {
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
        }

        // get payments
        List<Payment> payments = account.getPayments();
        if (payments != null) {
            for (Payment p : payments) {
                if (p.getTransactionDate().before(calendar)) {
                    balance -= p.getAmount();
                }
            }
        }
        return balance;
    }

    public RestResponse getAccountsReceivables(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_account_receivable");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                log.info("Getting account balances params");
                ReportsParam request = requestObject.getObject();
                Map<String, String> params = new HashMap<>();

                if (request.getFields() != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonString = mapper.writeValueAsString(request.getFields());
                    log.info("ParamsJSON:" + jsonString);
                    params = mapper.readValue(jsonString, Map.class);

                }

                log.info("Generating account balances report");
                List<Account> accounts;
                accounts = accountRepository.findAll();
                Double totalAmount = 0.0;
                if (!accounts.isEmpty()) {
                    log.info(accounts.size() + " accounts found.");
                    List<BalancesReport> balances = new ArrayList<>();

                    for (Account acc : accounts) {
                        BalancesReport br = new BalancesReport();
                        br.setAccName(acc.getAccName());
                        br.setAccNo(acc.getAccNo());
                        br.setZone(acc.getZone().getName());

                        br.setBalance(acc.getOutstandingBalance());

                        br.setActive(acc.isActive());


                        Boolean include = true;

                        if (params != null) {
                            if (!params.isEmpty()) {
                                //transactionDate=null, zoneId=7, accountStatus=Active, creditBalances=Exclude
                                //get balance based on transaction date

                                //zone id
                                if (params.containsKey("zoneId")) {
                                    Object zoneId = params.get("zoneId");
                                    if (acc.getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                        include = false;
                                    }
                                }
                                //account status
                                if (params.containsKey("accountStatus")) {
                                    String status = params.get("accountStatus");
                                    if (status.compareToIgnoreCase("inactive") == 0) {
                                        if (acc.isActive()) {
                                            include = false;
                                        }
                                    } else if (status.compareToIgnoreCase("active") == 0) {
                                        if (!acc.isActive()) {
                                            include = false;
                                        }
                                    }
                                }
                            }
                        }

                        if (include) {
                            //get balance based on date
                            if (params.containsKey("transactionDate")) {
                                Calendar calendar = Calendar.getInstance();
                                Object unixTime = params.get("transactionDate");
                                if (unixTime.toString().compareToIgnoreCase("null") != 0) {
                                    calendar.setTimeInMillis(Long.valueOf(unixTime.toString()) * 1000);
                                }

                                //update balance on local object
                                br.setBalance(this.getAccountBalanceByDate(acc, calendar));
                            }

                            if (br.getBalance() > 0) {
                                totalAmount += br.getBalance();
                                balances.add(br);
                            }
                        }
                    }
                    log.info("Packaged report data...");


                    ReportObject report = new ReportObject();
                    report.setAmount(totalAmount);
                    report.setDate(Calendar.getInstance());

                    accounts = null;

                    report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                    report.setTitle(this.optionService.getOption("REPORT:ACCOUNTS_RECEIVABLE").getValue());
                    report.setContent(balances);
                    log.info("Sending Payload send to client...");
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(report);
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

    public RestResponse getCreditBalances(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_credit_balances");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                log.info("Getting credit balances params");
                ReportsParam request = requestObject.getObject();
                Map<String, String> params = new HashMap<>();

                if (request.getFields() != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonString = mapper.writeValueAsString(request.getFields());
                    log.info("ParamsJSON:" + jsonString);
                    params = mapper.readValue(jsonString, Map.class);

                }

                log.info("Generating credit balances report");
                List<Account> accounts;
                accounts = accountRepository.findAll();
                Double totalAmount = 0.0;
                if (!accounts.isEmpty()) {
                    log.info(accounts.size() + " accounts found.");
                    List<BalancesReport> balances = new ArrayList<>();

                    for (Account acc : accounts) {
                        BalancesReport br = new BalancesReport();
                        br.setAccName(acc.getAccName());
                        br.setAccNo(acc.getAccNo());
                        br.setZone(acc.getZone().getName());
                        br.setBalance(acc.getOutstandingBalance());
                        br.setActive(acc.isActive());

                        Boolean include = true;
                        if (params != null) {
                            if (!params.isEmpty()) {
                                //zone id
                                if (params.containsKey("zoneId")) {
                                    Object zoneId = params.get("zoneId");
                                    if (acc.getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                        include = false;
                                    }
                                }
                                //account status
                                if (params.containsKey("accountStatus")) {
                                    String status = params.get("accountStatus");
                                    if (status.compareToIgnoreCase("inactive") == 0) {
                                        if (acc.isActive()) {
                                            include = false;
                                        }
                                    } else if (status.compareToIgnoreCase("active") == 0) {
                                        if (!acc.isActive()) {
                                            include = false;
                                        }
                                    }
                                }
                            }
                        }

                        if (include) {
                            //get balance based on date
                            if (params.containsKey("transactionDate")) {
                                Calendar calendar = Calendar.getInstance();
                                Object unixTime = params.get("transactionDate");
                                if (unixTime.toString().compareToIgnoreCase("null") != 0) {
                                    calendar.setTimeInMillis(1000 * Long.valueOf(unixTime.toString()));
                                }

                                //update balance on local object
                                br.setBalance(this.getAccountBalanceByDate(acc, calendar));
                            }

                            if (br.getBalance() < 0) {
                                totalAmount += (br.getBalance());
                                balances.add(br);
                            }
                        }
                    }
                    log.info("Packaged report data...");


                    ReportObject report = new ReportObject();
                    report.setAmount(totalAmount);
                    report.setDate(Calendar.getInstance());

                    accounts = null;

                    report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                    report.setTitle(this.optionService.getOption("REPORT:CREDIT_BALANCES").getValue());
                    report.setContent(balances);
                    log.info("Sending Payload send to client...");
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(report);
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

    public RestResponse getFieldCardReport(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_field_report");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                ReportsParam request = requestObject.getObject();
                Map<String, String> params = new HashMap<>();

                if (request.getFields() != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonString = mapper.writeValueAsString(request.getFields());
                    params = mapper.readValue(jsonString, Map.class);
                }

                List<Account> accounts;
                accounts = accountRepository.findAll();

                if (!accounts.isEmpty()) {
                    log.info(accounts.size() + " accounts found.");
                    List<AccountRecord> accountRecords = new ArrayList<>();

                    for (Account acc : accounts) {
                        AccountRecord ar = new AccountRecord();
                        ar.setAccName(acc.getAccName());
                        ar.setAccNo(acc.getAccNo());
                        ar.setZone(acc.getZone().getName());
                        ar.setLocation(acc.getLocation().getName());
                        ar.setActive(acc.isActive());

                        if (acc.isMetered()) {
                            ar.setMeterNo(acc.getMeter().getMeterNo());
                            ar.setMeterOwner(acc.getMeter().getMeterOwner().getName());
                        }


                        Boolean include = true;
                        if (params != null) {
                            if (!params.isEmpty()) {
                                //zone id
                                if (params.containsKey("zoneId")) {
                                    Object zoneId = params.get("zoneId");
                                    if (acc.getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                        include = false;
                                    }
                                }

                                //account status
                                if (params.containsKey("accountStatus")) {
                                    String status = params.get("accountStatus");
                                    if (status.compareToIgnoreCase("inactive") == 0) {
                                        if (acc.isActive()) {
                                            include = false;
                                        }
                                    } else if (status.compareToIgnoreCase("active") == 0) {
                                        if (!acc.isActive()) {
                                            include = false;
                                        }
                                    }
                                }
                            }
                        }

                        if (include) {
                            accountRecords.add(ar);
                        }
                    }
                    log.info("Packaged report data...");

                    ReportObject report = new ReportObject();
                    report.setDate(Calendar.getInstance());
                    accounts = null;
                    report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                    report.setTitle(this.optionService.getOption("REPORT:FIELD_CARD").getValue());
                    report.setContent(accountRecords);
                    log.info("Sending Payload send to client...");
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(report);
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
