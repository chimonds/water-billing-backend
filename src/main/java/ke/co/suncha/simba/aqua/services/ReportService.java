package ke.co.suncha.simba.aqua.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.reports.*;
import ke.co.suncha.simba.aqua.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 5/6/15.
 */
@Service
@Scope("prototype")
public class ReportService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private BillItemTypeRepository billItemTypeRepository;

    @Autowired
    private BillingMonthRepository billingMonthRepository;

    @Autowired
    private BillRepository billRepository;

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

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public ReportService() {
    }

    public RestResponse getBillingSummary(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                log.info("Getting billing summary report...");
                ReportsParam request = requestObject.getObject();

                Map<String, String> params = this.getParamsMap(request);

                BillingSummaryRecord bsr = new BillingSummaryRecord();

                List<BillItemType> billItemTypes = billItemTypeRepository.findAll();
                if (!billItemTypes.isEmpty()) {
                    Integer x = 0;
                    for (BillItemType bit : billItemTypes) {
                        //set amount to zero
                        bit.setAmount(0.0);
                        billItemTypes.set(x, bit);
                        x++;
                    }
                }

                List<Bill> bills;
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

                    for (Bill b : bills) {
                        Boolean include = true;
                        if (params.containsKey("zoneId")) {
                            Object zoneId = params.get("zoneId");
                            if (b.getAccount().getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                include = false;
                            }
                        }


                        if (include) {

                            //amount billed
                            if (b.getConsumptionType().compareToIgnoreCase("Actual") == 0) {
                                bsr.setBilledOnActual(bsr.getBilledOnActual() + b.getAmount());
                            } else if (b.getConsumptionType().compareToIgnoreCase("Average") == 0) {
                                bsr.setBilledOnEstimate(bsr.getBilledOnEstimate() + b.getAmount());
                            }

                            //Other charges
                            if (!b.getBillItems().isEmpty()) {
                                for (BillItem bi : b.getBillItems()) {
                                    if (bi.getBillItemType().getName().compareToIgnoreCase("Reconnection Fee") == 0) {
                                        bsr.setReconnectionFee(bsr.getReconnectionFee() + bi.getAmount());
                                    } else if (bi.getBillItemType().getName().compareToIgnoreCase("At Owners Request Fee") == 0) {
                                        bsr.setAtOwnersRequestFee(bsr.getAtOwnersRequestFee() + bi.getAmount());
                                    } else if (bi.getBillItemType().getName().compareToIgnoreCase("Change Of Account Name") == 0) {
                                        bsr.setChangeOfAccountName(bsr.getChangeOfAccountName() + bi.getAmount());
                                    } else if (bi.getBillItemType().getName().compareToIgnoreCase("By Pass Fee") == 0) {
                                        bsr.setByPassFee(bsr.getByPassFee() + bi.getAmount());
                                    } else if (bi.getBillItemType().getName().compareToIgnoreCase("Bounced Cheque Fee") == 0) {
                                        bsr.setBouncedChequeFee(bsr.getBouncedChequeFee() + bi.getAmount());
                                    } else if (bi.getBillItemType().getName().compareToIgnoreCase("Surcharge Irrigation") == 0) {
                                        bsr.setSurchargeIrrigation(bsr.getSurchargeIrrigation() + bi.getAmount());
                                    } else if (bi.getBillItemType().getName().compareToIgnoreCase("Surcharge Missuse") == 0) {
                                        bsr.setSurchargeMissuse(bsr.getSurchargeMissuse() + bi.getAmount());
                                    } else if (bi.getBillItemType().getName().compareToIgnoreCase("Meter Servicing") == 0) {
                                        bsr.setMeterServicing(bsr.getMeterServicing() + bi.getAmount());
                                    }
                                }
                            }
                        }
                    }
                    List<Payment> payments;
                    payments = paymentRepository.findByBillingMonth(billingMonth);

                    if (!payments.isEmpty()) {

                        for (Payment p : payments) {
                            //payments
                            Boolean include = true;
                            if (params.containsKey("zoneId")) {
                                Object zoneId = params.get("zoneId");
                                if (p.getAccount().getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                    include = false;
                                }
                            }

                            if (include) {
                                if (p.getPaymentType().getName().compareToIgnoreCase("Credit") == 0) {
                                    bsr.setCreditAdjustments(bsr.getCreditAdjustments() + Math.abs(p.getAmount()));
                                } else if (p.getPaymentType().getName().compareToIgnoreCase("Debit") == 0) {
                                    bsr.setDebitAdjustments(bsr.getDebitAdjustments() + Math.abs(p.getAmount()));
                                }
                            }
                        }
                    }

                    //send report
                    ReportObject report = new ReportObject();
                    report.setDate(Calendar.getInstance());

                    report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                    report.setTitle(this.optionService.getOption("REPORT:BILLING_SUMMARY").getValue());
                    report.setContent(bsr);

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

    public RestResponse getAccountStatement(RestRequestObject<ReportsParam> requestObject, Long accountId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                log.info("Getting account statement report...");
                ReportsParam request = requestObject.getObject();

                Map<String, String> params = this.getParamsMap(request);

                log.info("Getting account statement by account");
                Account account;

                account = accountRepository.findOne(Long.valueOf(accountId));
                if (account == null) {
                    responseObject.setMessage("Invalid account.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                //fill report
                List<StatementRecord> records = new ArrayList<StatementRecord>();

                //balance brought forward
                StatementRecord balanceBf = new StatementRecord();
                balanceBf.setTransactionDate(account.getCreatedOn());
                balanceBf.setItemType("Balance B/f");
                balanceBf.setRefNo("");
                balanceBf.setAmount(account.getBalanceBroughtForward());
                records.add(balanceBf);

                //add bills
                if (!account.getBills().isEmpty()) {
                    for (Bill bill : account.getBills()) {
                        //add bill record
                        Calendar billingMonth = bill.getBillingMonth().getMonth();

                        String billingYearMonth = billingMonth.get(Calendar.YEAR) + " " + billingMonth.get(Calendar.MONTH);

                        StatementRecord billRecord = new StatementRecord();
                        billRecord.setTransactionDate(bill.getTransactionDate());
                        billRecord.setItemType("Bill");
                        billRecord.setRefNo(billingYearMonth);
                        billRecord.setAmount(bill.getAmount());
                        records.add(billRecord);

                        //get billing items
                        if (!bill.getBillItems().isEmpty()) {
                            for (BillItem billItem : bill.getBillItems()) {
                                StatementRecord billItemRecord = new StatementRecord();
                                billItemRecord.setTransactionDate(bill.getTransactionDate());
                                billItemRecord.setItemType("Charge");
                                billItemRecord.setRefNo(billingYearMonth);
                                billItemRecord.setAmount(billItem.getAmount());
                                records.add(billItemRecord);
                            }
                        }
                    }
                }

                //add payments
                if (!account.getPayments().isEmpty()) {
                    for (Payment payment : account.getPayments()) {
                        StatementRecord paymentRecord = new StatementRecord();
                        paymentRecord.setTransactionDate(payment.getTransactionDate());
                        paymentRecord.setItemType(payment.getPaymentType().getName());
                        paymentRecord.setRefNo(payment.getReceiptNo() + "-" + payment.getPaymentType().getName());
                        paymentRecord.setAmount(payment.getAmount());

                        Double amount = Math.abs(payment.getAmount());

                        if (!payment.getPaymentType().isNegative()) {
                            paymentRecord.setAmount(amount * -1);
                        }
                        records.add(paymentRecord);
                    }
                }

                if (!records.isEmpty()) {
                    //Sort collection by transaction date
                    Collections.sort(records);

                    Double runningTotal = 0.0;
                    //calculate running totals
                    Integer location = 0;
                    for (StatementRecord record : records) {
                        runningTotal += record.getAmount();
                        record.setRunningAmount(runningTotal);
                        records.set(location, record);
                        location++;
                    }
                }

                log.info("Done crunching Statement report data...");

                ReportObject report = new ReportObject();
                report.setAmount(0.0);
                report.setDate(Calendar.getInstance());
                account = null;
                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                report.setTitle(this.optionService.getOption("REPORT:ACCOUNT_STATEMENT").getValue());
                report.setContent(records);
                log.info("Sending Payload send to client...");
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

    public RestResponse getPayments(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                log.info("Getting payments report...");
                ReportsParam request = requestObject.getObject();

                Map<String, String> params = this.getParamsMap(request);


                log.info("Getting payments by date");
                List<Payment> payments;

                //get payments from date
                Calendar fromDate = Calendar.getInstance();

                if (params.containsKey("fromDate")) {
                    Object unixTime = params.get("fromDate");
                    if (unixTime.toString().compareToIgnoreCase("null") != 0) {
                        fromDate.setTimeInMillis(Long.valueOf(unixTime.toString()));
                    }
                }

                //get payments to a specific date
                Calendar toDate = Calendar.getInstance();
                if (params.containsKey("toDate")) {
                    Object unixTime = params.get("toDate");
                    if (unixTime.toString().compareToIgnoreCase("null") != 0) {
                        toDate.setTimeInMillis(Long.valueOf(unixTime.toString()));
                    }
                }

                //get payments
                payments = paymentRepository.findByTransactionDateBetweenOrderByTransactionDateDesc(fromDate, toDate);

                Double totalAmount = 0.0;
                if (!payments.isEmpty()) {
                    log.info(payments.size() + " payments found.");

                    List<PaymentRecord> records = new ArrayList<>();

                    for (Payment p : payments) {
                        Boolean include = true;
                        //Filter based on zone
                        if (params.containsKey("zoneId")) {
                            Object zoneId = params.get("zoneId");
                            if (p.getAccount().getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                include = false;
                            }
                        }

                        //Filter based on payment type
                        if (params.containsKey("paymentTypeId")) {
                            Object paymentTypeId = params.get("paymentTypeId");
                            if (p.getPaymentType().getPaymentTypeId() != Long.valueOf(paymentTypeId.toString())) {
                                include = false;
                            }
                        }

                        //check if to include in payload
                        if (include) {
                            PaymentRecord pr = new PaymentRecord();
                            pr.setAccName(p.getAccount().getAccName());
                            pr.setAccNo(p.getAccount().getAccNo());
                            pr.setZone(p.getAccount().getZone().getName());
                            pr.setReceiptNo(p.getReceiptNo());
                            pr.setTransactionDate(p.getTransactionDate());
                            pr.setAmount(p.getAmount());
                            pr.setPaymentType(p.getPaymentType().getName());
                            records.add(pr);

                            //add
                            totalAmount += p.getAmount();
                        }
                    }
                    log.info("Done crunching payments report data...");

                    ReportObject report = new ReportObject();
                    report.setAmount(totalAmount);
                    report.setDate(Calendar.getInstance());
                    payments = null;
                    report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                    report.setTitle(this.optionService.getOption("REPORT:PAYMENTS").getValue());
                    report.setContent(records);
                    log.info("Sending Payload send to client...");
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(report);
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

    private Map<String, String> getParamsMap(ReportsParam request) {
        Map<String, String> params = new HashMap<>();
        try {
            if (request.getFields() != null) {
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writeValueAsString(request.getFields());
                log.info("Report parameters:" + jsonString);
                params = mapper.readValue(jsonString, Map.class);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return params;
    }

}
