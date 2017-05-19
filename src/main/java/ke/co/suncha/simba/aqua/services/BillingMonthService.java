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

import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.aqua.models.BillingMonth;
import ke.co.suncha.simba.aqua.options.SystemOptionService;
import ke.co.suncha.simba.aqua.reports.scheduled.ReportHeader;
import ke.co.suncha.simba.aqua.reports.scheduled.ReportHeaderRepository;
import ke.co.suncha.simba.aqua.reports.scheduled.ReportHeaderService;
import ke.co.suncha.simba.aqua.reports.scheduled.ReportType;
import ke.co.suncha.simba.aqua.reports.scheduled.monthly.SystemReport;
import ke.co.suncha.simba.aqua.reports.scheduled.monthly.SystemReportService;
import ke.co.suncha.simba.aqua.repository.BillingMonthRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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

import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Service
public class BillingMonthService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BillingMonthRepository billingMonthRepository;

    @Autowired
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private SystemOptionService systemOptionService;

    @Autowired
    private ReportHeaderRepository reportHeaderRepository;

    @Autowired
    private SystemReportService systemReportService;

    @Autowired
    private ReportHeaderService reportHeaderService;


    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public BillingMonthService() {

    }

    @Transactional
    public Boolean canTransact(DateTime transDate) {
        //check if more than one billing months is open
        if (billingMonthRepository.countWithCurrent(1) != 1) {
            return Boolean.FALSE;
        }

        BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);
        if (billingMonth == null) {
            return Boolean.FALSE;
        }

        //Check if strict transaction mode enabled
        if (systemOptionService.isStrictModeEnabled()) {
            DateTime today = new DateTime();
            DateTime lastDayOfTheMonth = today.dayOfMonth().withMaximumValue().withTimeAtStartOfDay().hourOfDay().withMaximumValue();
            DateTime firstDayOfTheMonth = today.dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            DateTime billingDate = new DateTime();
            billingDate = billingDate.withMillis(billingMonth.getMonth().getMillis());

            //Check if billing month is between first and last day of the month
            if (billingDate.isBefore(firstDayOfTheMonth) || billingDate.isAfter(lastDayOfTheMonth)) {
                //set billing month active to false
                //billingMonth.setCurrent(0);
                //billingMonth = billingMonthRepository.save(billingMonth);
                return Boolean.FALSE;
            }

            if (transDate.isBefore(firstDayOfTheMonth) || transDate.isAfter(lastDayOfTheMonth)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    //second, minute, hour, day of month, month, day(s) of week
    //	0 0 0 1/1 * ? * every day at midnight
    //	0 0 0 1 1/1 ? * every first day of the month
    @Scheduled(cron = "0 0 0 1 * *")
    public void validateBillingMonths() {
        log.info("CRON REPORT RUN @::" + new DateTime());
        runTasks();
    }

    @Transactional
    public void runTasks() {
        try {
            //check if there is an active month and close it
            BillingMonth billingMonth = getActiveMonth();
            if (billingMonth != null) {
                billingMonth.setCurrent(0);
                billingMonth = billingMonthRepository.save(billingMonth);
            }

            DateTime today = new DateTime();
            DateTime lastDayOfTheMonth = today.withTimeAtStartOfDay();
            lastDayOfTheMonth = lastDayOfTheMonth.minusSeconds(1);


            //run ageing
            ReportHeader ageingHeader = new ReportHeader();
            ageingHeader.setSystem(Boolean.TRUE);
            ageingHeader.setToDate(lastDayOfTheMonth.withZone(DateTimeZone.forID("Africa/Nairobi")));
            ageingHeader.setReportType(ReportType.AGEING);
            ageingHeader.setRequestedBy("system");
            ageingHeader.setCreatedOn(new DateTime());
            ageingHeader = reportHeaderService.create(ageingHeader);
            //add to queue
            reportHeaderService.addToQueue(ageingHeader);


            //run balances
            ReportHeader balancesHeader = new ReportHeader();
            balancesHeader.setSystem(Boolean.TRUE);
            balancesHeader.setToDate(lastDayOfTheMonth.withZone(DateTimeZone.forID("Africa/Nairobi")));
            balancesHeader.setReportType(ReportType.ACCOUNT_BALANCES);
            balancesHeader.setRequestedBy("system");
            balancesHeader.setCreatedOn(new DateTime());
            balancesHeader = reportHeaderService.create(balancesHeader);
            reportHeaderService.addToQueue(balancesHeader);


            //get the next month in cycle and open it
            Long id = 0l;
            if (billingMonth != null) {
                id = billingMonth.getBillingMonthId();
                id++;
            }

            //save system report object
            SystemReport systemReport = new SystemReport();
            systemReport.setAgeingHeaderId(ageingHeader.getReportHeaderId());
            systemReport.setBalancesHeaderId(balancesHeader.getReportHeaderId());
            systemReport.setMonthToOpen(id);
            systemReportService.create(systemReport);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());
        }
    }

    public BillingMonth getActiveMonth() {
        BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);
        if (billingMonth != null) {
            return billingMonth;
        }
        return null;
    }

    public BillingMonth getById(Long billingMonthId) {
        return billingMonthRepository.findOne(billingMonthId);
    }

    @Transactional
    public RestResponse update(RestRequestObject<BillingMonth> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "billing_month_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                BillingMonth billingMonth = requestObject.getObject();
                BillingMonth bm = billingMonthRepository.findOne(billingMonth.getBillingMonthId());
                if (bm == null) {
                    responseObject.setMessage("Billing month not found");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else {

                    if (billingMonth.getCurrent() == 1) {
                        if (billingMonthRepository.countWithCurrent(1) > 0) {
                            responseObject.setMessage("Please close all billing dates before opening a new billing date");
                            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        } else if (systemOptionService.isStrictModeEnabled()) {
                            responseObject.setMessage("Sorry we can not complete your request. Strct mode is enabled.");
                            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        } else {

                            // Open month
                            bm.setCurrent(billingMonth.getCurrent());
                            // save
                            billingMonthRepository.save(bm);
                            responseObject.setMessage("Billing month updated successfully");
                            responseObject.setPayload(bm);
                            response = new RestResponse(responseObject, HttpStatus.OK);

                            //Start - audit trail
                            AuditRecord auditRecord = new AuditRecord();
                            auditRecord.setParentID(String.valueOf(bm.getBillingMonthId()));
                            auditRecord.setParentObject("BILLING MONTH");
                            auditRecord.setCurrentData(bm.toString());
                            auditRecord.setNotes("CREATED BILLING MONTH");
                            auditService.log(AuditOperation.CREATED, auditRecord);
                            //End - audit trail
                        }
                    } else {
                        // close month
                        bm.setCurrent(billingMonth.getCurrent());

                        // save
                        billingMonthRepository.save(bm);
                        responseObject.setMessage("Billing month updated successfully");
                        responseObject.setPayload(bm);
                        response = new RestResponse(responseObject, HttpStatus.OK);

                        //Start - audit trail
                        AuditRecord auditRecord = new AuditRecord();
                        auditRecord.setParentID(String.valueOf(bm.getBillingMonthId()));
                        auditRecord.setParentObject("BILLING MONTH");
                        auditRecord.setCurrentData(bm.toString());
                        auditRecord.setNotes("CLOSED/OPENED BILLING MONTH");
                        auditService.log(AuditOperation.UPDATED, auditRecord);
                        //End - audit trail
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

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "month");
    }

    public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "billing_month_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest p = requestObject.getObject();

                Page<BillingMonth> pageOfObjects;

                pageOfObjects = billingMonthRepository.findByIsEnabled(1, new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));

                if (pageOfObjects.hasContent()) {

                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(pageOfObjects);
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

    public RestResponse getActiveBillingMonth(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "billing_month_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);
                if (billingMonth != null) {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(billingMonth);
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

    public RestResponse getAll(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "billing_month_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest p = requestObject.getObject();

                List<BillingMonth> billingMonths;

                billingMonths = billingMonthRepository.findAllByIsEnabledOrderByMonthDesc(1);
                if (!billingMonths.isEmpty()) {

                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(billingMonths);
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
