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

import com.fasterxml.jackson.databind.ObjectMapper;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.reports.*;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.repository.BillItemRepository;
import ke.co.suncha.simba.aqua.repository.BillRepository;
import ke.co.suncha.simba.aqua.repository.BillingMonthRepository;

import ke.co.suncha.simba.aqua.utils.BillMeta;
import ke.co.suncha.simba.aqua.utils.BillRequest;
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

import java.util.*;

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

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public BillService() {
    }

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    private BillingMonth getActiveBillingMonth() {
        BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);

        if (billingMonth != null) {
            //set billing code
            String year = String.valueOf(billingMonth.getMonth().get(Calendar.YEAR));
            String month = String.valueOf(billingMonth.getMonth().get(Calendar.MONTH));
            if (month.length() == 1) {
                month = "0" + month;
            }
            String billingCode = year + month;
            billingMonth.setCode(Integer.valueOf(billingCode));
            log.info("Billing code:" + billingCode);
        }

        return billingMonth;
    }

    public RestResponse bill(RestRequestObject<BillRequest> requestObject, Long accountId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_bill");
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

                log.info("Billing account:" + account.getAccNo());

                Bill lastBill = this.getAccountLastBill(accountId);
                if (lastBill.isBilled()) {
                    responseObject.setMessage("The account has already being billed this month.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                BillRequest billRequest = requestObject.getObject();

                Bill bill = new Bill();

                //set
                //TODO; check nulls
                bill.setCurrentReading(billRequest.getCurrentReading());
                bill.setPreviousReading(billRequest.getPreviousReading());


                //get units consumed
                Integer unitsConsumed = bill.getCurrentReading() - bill.getPreviousReading();
                if (unitsConsumed > 0) {
                    bill.setConsumptionType("Actual");
                } else {
                    unitsConsumed = lastBill.getAverageConsumption();
                    bill.setConsumptionType("Average");
                }

                //set units consumed
                bill.setUnitsBilled(unitsConsumed);

                //set meter rent
                if (account.isMetered()) {
                    if (account.getMeter().getMeterOwner().isChargable()) {
                        bill.setMeterRent(account.getMeter().getMeterSize().getRentAmount());
                    }
                }

                //set billing amount
                BillMeta billMeta = new BillMeta();
                billMeta.setUnits(unitsConsumed);
                billMeta = tariffService.calculate(billMeta, accountId);

                bill.setAmount(billMeta.getAmount());
                bill.setContent(billMeta.getContent());

                //check billed amount
                if (bill.getAmount() <= 0) {
                    responseObject.setMessage("Sorry we could not save the bill. Invalid billing amount.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                //set billcode
                //String billCode =
                BillingMonth activeBillingMonth = this.getActiveBillingMonth();
                bill.setBillingMonth(activeBillingMonth);

                bill.setBillCode(activeBillingMonth.getCode());

                //set average
                bill.setAverageConsumption(account.getAverageConsumption());

                //
                log.info("adding bill to data store");

                //set account
                bill.setAccount(account);
                Bill createdBill = billRepository.save(bill);

                log.info("Applying charges to bill...");
                if (billRequest.getBillItemTypes() != null) {
                    if (!billRequest.getBillItemTypes().isEmpty()) {
                        List<BillItem> billItems = new ArrayList<>();
                        for (BillItemType bit : billRequest.getBillItemTypes()) {
                            BillItem bi = new BillItem();
                            bi.setAmount(bit.getAmount());
                            bi.setBill(createdBill);
                            bi.setBillItemType(bit);

                            BillItem createdBillItem = billItemRepository.save(bi);

                            //add to list
                            log.info("Adding other charges:" + bit.getName());
                            billItems.add(createdBillItem);
                        }
                        bill.setBillItems(billItems);
                    }
                }

                //
                Double totalAmount = 0.0;
                if (account.isMetered()) {
                    if (account.getMeter().getMeterOwner().isChargable()) {
                        totalAmount += account.getMeter().getMeterSize().getRentAmount();
                    }
                }
                totalAmount += createdBill.getAmount();

                if (createdBill.getBillItems() != null) {
                    if (!createdBill.getBillItems().isEmpty()) {
                        for (BillItem bi : createdBill.getBillItems()) {
                            totalAmount += bi.getAmount();
                        }
                    }
                }
                //update total amount
                createdBill.setTotalBilled(totalAmount);

                createdBill = billRepository.save(bill);

                //save outsatanding balance
                account = accountRepository.findOne(accountId);
                account.setOutstandingBalance(paymentService.getAccountBalance(account));
                accountRepository.save(account);

                log.info("Account billed successfully:" + account.getAccNo());

                responseObject.setMessage("Account billed successfully");
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

    public RestResponse getAllByAccount(RestRequestObject<RestPageRequest> requestObject, Long account_id) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_bills");
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

    private Bill getAccountLastBill(Long accountId) {
        Bill lastBill = new Bill();
        // get current billing month
        BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);

        Account account = accountRepository.findOne(accountId);

        Page<Bill> bills;
        bills = billRepository.findByAccountOrderByBillCodeDesc(account, new PageRequest(0, 1));

        if (!bills.hasContent()) {
            // seems its iniatial bill so check if account is metered
            if (account.isMetered()) {
                lastBill.setCurrentReading(account.getMeter().getInitialReading());
            } else {
                // TODO;
            }
        } else {

            lastBill = bills.getContent().get(0);
            log.info("Most current bill:" + lastBill.getBillingMonth().getMonth().get(Calendar.YEAR) + "-" + lastBill.getBillingMonth().getMonth().get(Calendar.MONTH));
            log.info("Billing month:" + billingMonth.getMonth().get(Calendar.YEAR) + "-" + lastBill.getBillingMonth().getMonth().get(Calendar.MONTH));

            if (lastBill.getBillingMonth().getMonth().before(billingMonth.getMonth())) {
                lastBill.setBilled(false);
            } else {
                lastBill.setBilled(true);
            }

        }
        return lastBill;
    }

    public RestResponse getLastBill(RestRequestObject<RestPageRequest> requestObject, Long accountId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "bills_last");
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

    public RestResponse getMeterReadingsReport(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_meter_readings");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                ReportsParam request = requestObject.getObject();
                Map<String, String> params = new HashMap<>();

                if (request.getFields() != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonString = mapper.writeValueAsString(request.getFields());
                    log.info("jsonString:" + jsonString);
                    params = mapper.readValue(jsonString, Map.class);
                }
                log.info("Generating meter reading report...");
                List<Bill> bills;
                List<MeterRecord> meterRecords = new ArrayList<>();

                if (params == null || params.isEmpty()) {
                    responseObject.setMessage("Please select billing month.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }


                if (params.containsKey("billingMonthId")) {
                    Object billingMonthId = params.get("billingMonthId");

                    BillingMonth billingMonth;
                    billingMonth = billingMonthRepository.findOne(Long.valueOf(billingMonthId.toString()));

                    if (billingMonth == null) {
                        responseObject.setMessage("Invalid billing month.");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }

                    bills = billRepository.findAllByBillingMonth(billingMonth);
                    log.info("Bills " + bills.size() + " found.");
                    if (bills == null || bills.isEmpty()) {
                        responseObject.setMessage("No content found");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }
                    Integer totalUnits = 0;
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

                        Boolean include = true;
                        //zone id
                        if (params.containsKey("zoneId")) {
                            Object zoneId = params.get("zoneId");
                            if (b.getAccount().getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                include = false;
                            }
                        }

                        if (include) {
                            totalUnits += mr.getUnits();
                            meterRecords.add(mr);
                        }

                    }

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

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getMeterStopsReport(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_meter_stops");
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

                List<Bill> bills;
                List<MeterRecord> meterRecords = new ArrayList<>();

                if (params != null) {
                    if (!params.isEmpty()) {

                        if (params.containsKey("billingMonthId")) {
                            Object billingMonthId = params.get("billingMonthId");

                            BillingMonth billingMonth;
                            billingMonth = billingMonthRepository.findOne(Long.valueOf(billingMonthId.toString()));

                            if (billingMonth == null) {
                                responseObject.setMessage("Invalid billing month.");
                                response = new RestResponse(responseObject, HttpStatus.NO_CONTENT);
                                return response;
                            }

                            bills = billRepository.findAllByBillingMonth(billingMonth);
                            log.info("Bills " + bills.size() + " found.");
                            if (bills == null || bills.isEmpty()) {
                                responseObject.setMessage("No content found");
                                response = new RestResponse(responseObject, HttpStatus.NO_CONTENT);
                                return response;
                            }
                            Integer totalUnits = 0;
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

                                Boolean include = true;
                                if (params.containsKey("zoneId")) {
                                    Object zoneId = params.get("zoneId");
                                    if (b.getAccount().getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                        include = false;
                                    }
                                }

                                if (include) {
                                    Integer units = mr.getCurrentReading() - mr.getPreviousReading();
                                    if (units == 0) {
                                        totalUnits += mr.getUnits();
                                        meterRecords.add(mr);
                                    }
                                }
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

    public RestResponse getNegativeReadingsReport(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_negative_readings");
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

                List<Bill> bills;
                List<MeterRecord> meterRecords = new ArrayList<>();

                if (params != null) {
                    if (!params.isEmpty()) {

                        if (params.containsKey("billingMonthId")) {
                            Object billingMonthId = params.get("billingMonthId");

                            BillingMonth billingMonth;
                            billingMonth = billingMonthRepository.findOne(Long.valueOf(billingMonthId.toString()));

                            if (billingMonth == null) {
                                responseObject.setMessage("Invalid billing month.");
                                response = new RestResponse(responseObject, HttpStatus.NO_CONTENT);
                                return response;
                            }

                            bills = billRepository.findAllByBillingMonth(billingMonth);
                            log.info("Bills " + bills.size() + " found.");
                            if (bills == null || bills.isEmpty()) {
                                responseObject.setMessage("No content found");
                                response = new RestResponse(responseObject, HttpStatus.NO_CONTENT);
                                return response;
                            }
                            Integer totalUnits = 0;
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

                                Boolean include = true;
                                if (params.containsKey("zoneId")) {
                                    Object zoneId = params.get("zoneId");
                                    if (b.getAccount().getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                        include = false;
                                    }
                                }

                                if (include) {
                                    Integer units = mr.getCurrentReading() - mr.getPreviousReading();
                                    if (units < 0) {
                                        totalUnits += mr.getUnits();
                                        meterRecords.add(mr);
                                    }
                                }
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

    public RestResponse getBilledAmountReport(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_billed_amount");
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

                List<Bill> bills;
                List<BillRecord> billRecords = new ArrayList<>();

                if (params == null || params.isEmpty()) {
                    responseObject.setMessage("Please select billing month.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (params.containsKey("billingMonthId")) {
                    Object billingMonthId = params.get("billingMonthId");

                    BillingMonth billingMonth;
                    billingMonth = billingMonthRepository.findOne(Long.valueOf(billingMonthId.toString()));

                    if (billingMonth == null) {
                        responseObject.setMessage("Invalid billing month.");
                        response = new RestResponse(responseObject, HttpStatus.NO_CONTENT);
                        return response;
                    }

                    bills = billRepository.findAllByBillingMonth(billingMonth);
                    log.info("Bills " + bills.size() + " found.");
                    if (bills == null || bills.isEmpty()) {
                        responseObject.setMessage("No content found");
                        response = new RestResponse(responseObject, HttpStatus.NO_CONTENT);
                        return response;
                    }
                    Integer totalUnits = 0;
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

                        Boolean include = true;
                        if (params.containsKey("zoneId")) {
                            Object zoneId = params.get("zoneId");
                            if (b.getAccount().getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                include = false;
                            }
                        }

                        if (include) {
                            totalUnits += br.getUnits();
                            totalAmountBilled += br.getAmountBilled();
                            totalMeterRent += br.getMeterRent();
                            totalCharges += br.getOtherCharges();
                            billRecords.add(br);
                        }
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


            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getBillingChecklistReport(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_billing_checklist");
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

                List<Bill> bills;
                List<BillRecord> billRecords = new ArrayList<>();

                if (params.isEmpty()) {
                    responseObject.setMessage("Please select billing month.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (params.containsKey("billingMonthId")) {
                    Object billingMonthId = params.get("billingMonthId");

                    BillingMonth billingMonth;
                    billingMonth = billingMonthRepository.findOne(Long.valueOf(billingMonthId.toString()));

                    if (billingMonth == null) {
                        responseObject.setMessage("Invalid billing month.");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }

                    bills = billRepository.findAllByBillingMonth(billingMonth);
                    log.info("Bills " + bills.size() + " found.");
                    if (bills == null || bills.isEmpty()) {
                        responseObject.setMessage("No content found");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }
                    Integer totalUnits = 0;
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

                        Boolean include = true;
                        if (params.containsKey("zoneId")) {
                            Object zoneId = params.get("zoneId");
                            if (b.getAccount().getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                include = false;
                            }
                        }

                        if (include) {
                            totalUnits += br.getUnits();
                            totalAmountBilled += br.getAmountBilled();
                            totalMeterRent += br.getMeterRent();
                            totalCharges += br.getOtherCharges();
                            billRecords.add(br);
                        }
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

            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

}
