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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 5/6/15.
 */
@Service
//@Scope("prototype")
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
    private AgeingRecordRepository ageingRecordRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SimbaOptionService optionService;

    @Autowired
    private AuthManager authManager;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BillService billService;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    AccountService accountService;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public ReportService() {
    }

    public RestResponse getMonthlyBills(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_monthly_bills");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }


                log.info("Getting Monthly Bills report...");
                ReportsParam request = requestObject.getObject();

                Map<String, String> params = this.getParamsMap(request);

                if (params.isEmpty() || !params.containsKey("billingMonthId")) {
                    responseObject.setMessage("Please select billing month.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                Object billingMonthId = params.get("billingMonthId");
                BillingMonth billingMonth;
                billingMonth = billingMonthRepository.findOne(Long.valueOf(billingMonthId.toString()));

                log.info("Getting bills for billing month:" + billingMonth.getMonth());
                if (billingMonth == null) {
                    responseObject.setMessage("Invalid billing month.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                String formatted = format1.format(billingMonth.getMonth().getTime());
                log.info("Getting bills for billing month:" + formatted);

                //get bills belonging to billing month
                List<Bill> bills = new ArrayList<>();

                if (params.containsKey("accNo")) {
                    //Get account number from params
                    String accNo = params.get("accNo").toString();
                    if (!accNo.isEmpty() && accNo != null) {
                        bills = billRepository.findAllByBillingMonthAndAccount_AccNo(billingMonth, accNo);
                        if (bills.isEmpty()) {
                            responseObject.setMessage("No bill found found for account number " + accNo);
                            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                            return response;
                        }
                    }
                } else {
                    bills = billRepository.findAllByBillingMonth(billingMonth);
                }

                log.info("Bills " + bills.size() + " found.");
                if (bills == null || bills.isEmpty()) {
                    responseObject.setMessage("No bills found");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                List<MonthlyBillRecord> records = new ArrayList<>();

                for (Bill b : bills) {
                    Boolean include = true;
                    if (params.containsKey("zoneId")) {
                        Object zoneId = params.get("zoneId");
                        if (b.getAccount().getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                            include = false;
                        }
                    }


                    if (include) {
                        MonthlyBillRecord monthlyBillRecord = new MonthlyBillRecord();
                        monthlyBillRecord.setAccName(b.getAccount().getAccName());
                        monthlyBillRecord.setAccNo(b.getAccount().getAccNo());
                        if (b.getAccount().isMetered()) {
                            monthlyBillRecord.setMeterNo(b.getAccount().getMeter().getMeterNo());
                            monthlyBillRecord.setMeterSize(b.getAccount().getMeter().getMeterSize().getSize());
                        }
                        monthlyBillRecord.setLocation(b.getAccount().getLocation().getName());
                        monthlyBillRecord.setBilledAmount(b.getAmount());


                        monthlyBillRecord.setConsumptionType(b.getConsumptionType());
                        monthlyBillRecord.setPreviousReading(b.getPreviousReading());
                        monthlyBillRecord.setCurrentReading(b.getCurrentReading());
                        monthlyBillRecord.setUnitsBilled(b.getUnitsBilled());
                        monthlyBillRecord.setBillingMonth(billingMonth.getMonth().getTime());
                        monthlyBillRecord.setTotalBilledAmount(b.getTotalBilled());

                        String[] billContent = b.getContent().split("#");
                        List<String> list = new ArrayList<>();
                        if (billContent.length > 0) {
                            for (String s : billContent) {
                                if (s.length() > 0) {
                                    list.add(s);
                                }
                            }
                        }
                        monthlyBillRecord.setBillSummaryList(list);
                        //payments
                        Double totalPayments = 0.0;
                        List<Payment> payments = paymentRepository.findByBillingMonthAndAccount(billingMonth, b.getAccount());
                        if (!payments.isEmpty()) {
                            List<PaymentRecord> paymentRecords = new ArrayList<>();

                            for (Payment p : payments) {
                                totalPayments += p.getAmount();
                                PaymentRecord paymentRecord = new PaymentRecord();
                                paymentRecord.setTransactionDate(p.getTransactionDate());
                                paymentRecord.setAmount(p.getAmount());
                                paymentRecord.setReceiptNo(p.getReceiptNo());
                                paymentRecords.add(paymentRecord);
                            }
                            monthlyBillRecord.setPayments(paymentRecords);

                        }
                        monthlyBillRecord.setTotalPayments(totalPayments);


                        //balance from last bill
                        Calendar date = billingMonth.getMonth();
                        date.set(Calendar.DATE, 1);
                        //date.add(Calendar.MONTH, -1);
                        //date.add(Calendar.MONTH, -1);
                        Double balanceBeforeBill = accountService.getAccountBalanceByDate(b.getAccount(), date);

                        Double paymentsOnBill = monthlyBillRecord.getTotalPayments();
                        //log.info("Balance before Bill:" + balanceBeforeBill);
                        //log.info("Payments on Bill:" + paymentsOnBill);

//                        if(paymentsOnBill>0) {
//                            monthlyBillRecord.setBalanceBf(balanceBeforeBill + paymentsOnBill);
//                        }
//                        else{
//                            //debit adjustment was done
//                            monthlyBillRecord.setBalanceBf(balanceBeforeBill);
//                        }

                        monthlyBillRecord.setBalanceBf(balanceBeforeBill);

                        //check if inarreas
                        if ((monthlyBillRecord.getBalanceBf() - monthlyBillRecord.getTotalPayments()) > 0) {
                            monthlyBillRecord.setInArreas(true);
                        }
                        // bill.balanceBf-bill.totalPayments

                        //Charges
                        List<ChargeRecord> chargeRecords = new ArrayList<>();
                        if (b.getMeterRent() > 0) {
                            ChargeRecord chargeRecord = new ChargeRecord();
                            chargeRecord.setName("Meter Rent");
                            chargeRecord.setAmount(b.getMeterRent());
                            chargeRecords.add(chargeRecord);
                        }

                        if (!b.getBillItems().isEmpty()) {
                            for (BillItem bi : b.getBillItems()) {
                                ChargeRecord chargeRecord = new ChargeRecord();
                                chargeRecord.setName(bi.getBillItemType().getName());
                                chargeRecord.setAmount(bi.getAmount());
                                chargeRecords.add(chargeRecord);
                            }
                        }


                        if (!chargeRecords.isEmpty()) {
                            monthlyBillRecord.setCharges(chargeRecords);
                        }

                        records.add(monthlyBillRecord);
                    }
                }


                //send report
                ReportObject report = new ReportObject();
                report.setDate(Calendar.getInstance());

                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                report.setTitle(this.optionService.getOption("REPORT:MONTHLY_BILLS").getValue());
                report.setContent(records);

                report.setHeading1(this.optionService.getOption("MONTHLY_WATER_BILL:HEADER1").getValue());
                report.setHeading2(this.optionService.getOption("MONTHLY_WATER_BILL:HEADER2").getValue());
                report.setHeading3(this.optionService.getOption("MONTHLY_WATER_BILL:HEADER3").getValue());
                report.setHeading4(this.optionService.getOption("MONTHLY_WATER_BILL:PAY_BEFORE").getValue());


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


    @Scheduled(fixedDelay = 30000)
    @Transactional
    private void populateAgeingReport() {
        try {
            log.info("Populating ageing report for accounts");
            List<Account> accounts = accountRepository.findAll();
            if (accounts.isEmpty()) {
                return;
            }
            log.info("Populating ageing report for accounts");
            //delete all ageing records
            ageingRecordRepository.deleteAll();

            for (Account acc : accounts) {
                AgeingRecord ageingRecord = new AgeingRecord();
                //log.info("Populating ageing report for:" + acc.getAccNo());
                try {
                    Long consumerId = accountRepository.findConsumerIdByAccountId(acc.getAccountId());
                    if (consumerId != null) {
                        Consumer consumer = consumerRepository.findOne(consumerId);
                        String fullName = "";
                        if (consumer.getFirstName() != null) {
                            fullName += consumer.getFirstName().toUpperCase() + " ";
                        }
                        if (consumer.getMiddleName() != null) {
                            fullName += consumer.getMiddleName().toUpperCase() + " ";
                        }
                        if (consumer.getLastName() != null) {
                            fullName += consumer.getLastName().toUpperCase() + " ";
                        }
                        ageingRecord.setName(fullName);
                    }
                } catch (Exception ex) {
                    log.info(ex.getMessage());
                }
                ageingRecord.setAccNo(acc.getAccNo());

                //set
                Calendar today = Calendar.getInstance();
                today.set(Calendar.DAY_OF_MONTH, 24);


                Calendar above180 = Calendar.getInstance();
                above180.set(Calendar.DAY_OF_MONTH, 24);
                above180.add(Calendar.MONTH, -6);
                above180.set(Calendar.DAY_OF_MONTH, 23);


                Calendar above120 = Calendar.getInstance();
                above120.set(Calendar.DAY_OF_MONTH, 24);
                above120.add(Calendar.MONTH, -4);
                above120.set(Calendar.DAY_OF_MONTH, 23);

                Calendar above90 = Calendar.getInstance();
                above90.set(Calendar.DAY_OF_MONTH, 24);
                above90.add(Calendar.MONTH, -3);
                above90.set(Calendar.DAY_OF_MONTH, 23);

                Calendar above60 = Calendar.getInstance();
                above60.set(Calendar.DAY_OF_MONTH, 24);
                above60.add(Calendar.MONTH, -2);
                above60.set(Calendar.DAY_OF_MONTH, 23);

                Calendar above30 = Calendar.getInstance();
                above30.set(Calendar.DAY_OF_MONTH, 24);
                above30.add(Calendar.MONTH, -1);
                above30.set(Calendar.DAY_OF_MONTH, 23);

                //get all payments to date
                Double allocationBalance = 0d;
                Double total_payments = paymentService.getAccountTotalPayments(acc.getAccountId());
                Double BILLS_NOT_PAID = 0d;

                //Start Bills above 180 days
                //Double billsAbove180Days = billService.getAccountBillsByDate(acc.getAccountId(), above180);
                Double billAbove180Days = billService.getAccountBillsByDate(acc.getAccountId(), above180);
                Double balance180days = 0d;
                BILLS_NOT_PAID = billAbove180Days;

                Double BILL_ABOVE_180_DAYS = billAbove180Days;
                Double balance_not_paid_above = BILL_ABOVE_180_DAYS;

                //if payments are greater or == to bills
                if (total_payments >= billAbove180Days) {
                    BILLS_NOT_PAID = 0d;
                    balance180days = 0d;//all bills have been paid
                    total_payments = total_payments - BILL_ABOVE_180_DAYS;
                }
                //payments less than bills but not zero
                else if (total_payments < billAbove180Days && total_payments > 0) {
                    BILLS_NOT_PAID = BILL_ABOVE_180_DAYS - total_payments;
                    balance180days = BILL_ABOVE_180_DAYS - total_payments;//all bills have been paid
                    //money finished
                    total_payments = 0d;
                }
                //no payment done
                else if (total_payments == 0d) {
                    BILLS_NOT_PAID = billAbove180Days;
                    balance180days = billAbove180Days;
                    total_payments = 0d;
                }

                if (balance_not_paid_above > 0) {

                }
                //Start Bills above 180 days

                //Start Bills above 120-180 days
                Double billAbove120Days = billService.getAccountBillsByDate(acc.getAccountId(), above120);
                Double balance120days = 0d;
                Double BILL_ABOVE_120_DAYS = BILLS_NOT_PAID;
                Double bills_not_paid_above_180_days = BILLS_NOT_PAID;

                if (billAbove120Days > billAbove180Days) {
                    BILL_ABOVE_120_DAYS = (billAbove120Days - billAbove180Days) + BILLS_NOT_PAID;
                    //BILL_ABOVE_120_DAYS = (billAbove120Days - billAbove180Days);
                    balance120days = BILL_ABOVE_120_DAYS;
                }


                //if payments are greater or == to bills
                if (total_payments >= BILL_ABOVE_120_DAYS) {
                    BILLS_NOT_PAID = 0d;
                    //all bills have been paid
                    balance120days = 0d;
                    total_payments = total_payments - BILL_ABOVE_120_DAYS;
                }
                //payments less than bills but not zero
                else if (total_payments < BILL_ABOVE_120_DAYS && total_payments > 0) {
                    BILLS_NOT_PAID = BILL_ABOVE_120_DAYS - total_payments;
                    balance120days = BILL_ABOVE_120_DAYS - total_payments;//all bills have been paid
                    //money finished
                    total_payments = 0d;
                }
                //no payment done
                else if (total_payments == 0d) {
                    BILLS_NOT_PAID = BILL_ABOVE_120_DAYS;// billAbove120Days;
                    total_payments = 0d;
                }


                if (bills_not_paid_above_180_days > 0 && balance120days > 0) {
                    balance120days = balance120days - bills_not_paid_above_180_days;
                }


                //End Bills above 120-180 days

                //Start Bills above 90-120 days
                Double billAbove90Days = billService.getAccountBillsByDate(acc.getAccountId(), above90);
                Double balance90days = 0d;

                Double BILL_ABOVE_90_DAYS = BILLS_NOT_PAID;
                Double bills_not_paid_above_90_days = BILLS_NOT_PAID;

                if (billAbove90Days > billAbove120Days) {
                    BILL_ABOVE_90_DAYS = (billAbove90Days - billAbove120Days) + BILLS_NOT_PAID;
                    balance90days = BILL_ABOVE_90_DAYS;
                }

                //if payments are greater or == to bills
                if (total_payments >= BILL_ABOVE_90_DAYS) {
                    BILLS_NOT_PAID = 0d;
                    //all bills have been paid
                    balance90days = 0d;
                    total_payments = total_payments - BILL_ABOVE_90_DAYS;
                }
                //payments less than bills but not zero
                else if (total_payments < BILL_ABOVE_90_DAYS && total_payments > 0) {
                    BILLS_NOT_PAID = BILL_ABOVE_90_DAYS - total_payments;
                    balance90days = BILL_ABOVE_90_DAYS - total_payments;//all bills have been paid
                    //money finished
                    total_payments = 0d;
                }
                //no payment done
                else if (total_payments == 0d) {
                    BILLS_NOT_PAID = BILL_ABOVE_90_DAYS;// billAbove90Days;
                    total_payments = 0d;
                }

                if (bills_not_paid_above_90_days > 0 && balance90days > 0) {
                    balance90days = balance90days - bills_not_paid_above_90_days;
                }
                //End Bills above 90-120 days

                //Start Bills above 60 days
                Double billAbove60Days = billService.getAccountBillsByDate(acc.getAccountId(), above60);
                Double balance60days = 0d;

                Double BILL_ABOVE_60_DAYS = BILLS_NOT_PAID;
                Double bills_not_paid_above_60_days = BILLS_NOT_PAID;

                if (billAbove60Days > billAbove90Days) {
                    BILL_ABOVE_60_DAYS = (billAbove60Days - billAbove90Days) + BILLS_NOT_PAID;
                    balance60days = BILL_ABOVE_60_DAYS;
                }

                //if payments are greater or == to bills
                if (total_payments >= BILL_ABOVE_60_DAYS) {
                    BILLS_NOT_PAID = 0d;
                    //billAbove60Days = 0;//all bills have been paid
                    balance60days = 0d;
                    total_payments = total_payments - BILL_ABOVE_60_DAYS;
                }
                //payments less than bills but not zero
                else if (total_payments < BILL_ABOVE_60_DAYS && total_payments > 0) {
                    BILLS_NOT_PAID = BILL_ABOVE_60_DAYS - total_payments;
                    balance60days = BILL_ABOVE_60_DAYS - total_payments;//all bills have been paid
                    //money finished
                    total_payments = 0d;
                }
                //no payment done
                else if (total_payments == 0) {
                    BILLS_NOT_PAID = BILL_ABOVE_60_DAYS;// billAbove60Days;
                    total_payments = 0d;
                }

                if (bills_not_paid_above_60_days > 0 && balance60days > 0) {
                    balance60days = balance60days - bills_not_paid_above_60_days;
                }
                //End Bills above 60 days

                //Start Bills above 30 days
                Double billAbove30Days = billService.getAccountBillsByDate(acc.getAccountId(), above30);
                Double balance30days = 0d;

                Double BILL_ABOVE_30_DAYS = BILLS_NOT_PAID;
                Double bills_not_above_30_days = BILLS_NOT_PAID;

                if (billAbove30Days > billAbove60Days) {
                    BILL_ABOVE_30_DAYS = (billAbove30Days - billAbove60Days) + BILLS_NOT_PAID;
                    balance30days = BILL_ABOVE_30_DAYS;
                }


                //if payments are greater or == to bills
                if (total_payments >= BILL_ABOVE_30_DAYS) {
                    BILLS_NOT_PAID = 0d;
                    //billAbove30Days = 0;//all bills have been paid
                    balance30days = 0d;
                    total_payments = total_payments - BILL_ABOVE_30_DAYS;
                }
                //payments less than bills but not zero
                else if (total_payments < BILL_ABOVE_30_DAYS && total_payments > 0) {
                    BILLS_NOT_PAID = BILL_ABOVE_30_DAYS - total_payments;
                    balance30days = BILL_ABOVE_30_DAYS - total_payments;//all bills have been paid
                    //money finished
                    total_payments = 0d;
                }
                //no payment done
                else if (total_payments == 0) {
                    BILLS_NOT_PAID = BILL_ABOVE_30_DAYS;
                    total_payments = 0d;
                }
                if (bills_not_above_30_days > 0 && balance30days > 0) {
                    balance30days = balance30days - bills_not_above_30_days;
                }
                //End Bills above 30 days

                //Start Bills above 0 days
                Double billAbove0Days = billService.getAccountBillsByDate(acc.getAccountId(), today);
                Double balance0days = 0d;

                Double BILL_ABOVE_0_DAYS = BILLS_NOT_PAID;
                Double bills_not_paid_above_0_days = BILLS_NOT_PAID;

                if (billAbove0Days > billAbove30Days) {
                    BILL_ABOVE_0_DAYS = (billAbove0Days - billAbove30Days) + BILLS_NOT_PAID;
                    balance0days = BILL_ABOVE_0_DAYS;
                } else {
                    BILL_ABOVE_0_DAYS = (billAbove0Days - billAbove30Days) + BILLS_NOT_PAID;
                    balance0days = BILL_ABOVE_0_DAYS;
                }

                //if payments are greater or == to bills
                if (total_payments >= BILL_ABOVE_0_DAYS) {
                    BILLS_NOT_PAID = 0d;
                    //billAbove0Days = 0;//all bills have been paid
                    balance0days = 0d;
                    total_payments = total_payments - BILL_ABOVE_0_DAYS;
                }
                //payments less than bills but not zero
                else if (total_payments < BILL_ABOVE_0_DAYS && total_payments > 0) {
                    BILLS_NOT_PAID = BILL_ABOVE_0_DAYS - total_payments;
                    balance0days = BILL_ABOVE_0_DAYS - total_payments;//all bills have been paid
                    //money finished
                    total_payments = 0d;
                }
                //no payment done
                else if (total_payments == 0) {
                    BILLS_NOT_PAID = BILL_ABOVE_0_DAYS;// billAbove0Days;
                    total_payments = 0d;
                }

                if (bills_not_paid_above_0_days > 0 && balance0days > 0) {
                    balance0days = balance0days - bills_not_paid_above_0_days;
                }
                //End Bills above 0 days


                Account account = accountRepository.findOne(acc.getAccountId());
                try {
                    if (account.getConsumer() != null) {
                        String fullName = "";
                        log.info(account.getConsumer().getConsumerId() + "");

                        if (account.getConsumer().getFirstName() != null) {
                            fullName = account.getConsumer().getFirstName();
                        }

                        if (account.getConsumer().getMiddleName() != null) {
                            fullName = fullName + " " + account.getConsumer().getMiddleName();
                        }

                        if (account.getConsumer().getLastName() != null) {
                            fullName = fullName + " " + account.getConsumer().getLastName();
                        }
                        ageingRecord.setName(fullName);
                    }
                } catch (Exception ex) {

                }

                if (account.getZone() != null) {
                    ageingRecord.setZone(account.getZone().getName());
                }


                ageingRecord.setAbove0(balance0days);
                ageingRecord.setAbove30(balance30days);
                ageingRecord.setAbove60(balance60days);
                ageingRecord.setAbove90(balance90days);
                ageingRecord.setAbove120(balance120days);
                ageingRecord.setAbove180(balance180days);
                ageingRecord.setAccount(acc);
                ageingRecord.setAccNo(acc.getAccNo());
                if (acc.isActive()) {
                    ageingRecord.setCutOff("Active");
                } else {
                    ageingRecord.setCutOff("Inactive");
                }

                ageingRecord.setBalance(paymentService.getAccountBalance(acc.getAccountId()));
                //save

                ageingRecordRepository.save(ageingRecord);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());
        }
    }

    public RestResponse getAgeingReport(RestRequestObject<ReportsParam> requestObject) {
        try {
            log.info("Generating ageing report");
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_ageing");
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

                List<AgeingRecord> ageingRecords;
                ageingRecords = ageingRecordRepository.findAll();

                if (!ageingRecords.isEmpty()) {
                    log.info(ageingRecords.size() + " ageing records found.");

                    List<AgeingRecord> records = new ArrayList<>();
                    for (AgeingRecord ageingRecord : ageingRecords) {

                        Boolean include = true;

                        if (params != null) {
                            if (!params.isEmpty()) {
                                //zone id
                                if (params.containsKey("zoneId")) {
                                    Object zoneId = params.get("zoneId");
                                    if (ageingRecord.getAccount().getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                        include = false;
                                    }
                                }

                                //account status
                                if (params.containsKey("accountStatus")) {
                                    String status = params.get("accountStatus");
                                    if (status.compareToIgnoreCase("inactive") == 0) {
                                        if (ageingRecord.getAccount().isActive()) {
                                            include = false;
                                        }
                                    } else if (status.compareToIgnoreCase("active") == 0) {
                                        if (!ageingRecord.getAccount().isActive()) {
                                            include = false;
                                        }
                                    }
                                }
                            }
                        }

                        if (include) {
                            records.add(ageingRecord);
                        }
                    }

                    log.info("Packaged report data...");
                    ReportObject report = new ReportObject();
                    report.setDate(Calendar.getInstance());
                    ageingRecords = null;
                    report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                    report.setTitle(this.optionService.getOption("REPORT:AGEING").getValue());
                    report.setContent(records);
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
            ex.printStackTrace();
        }
        return response;
    }

    public RestResponse getPotentialCutOff(RestRequestObject<ReportsParam> requestObject) {
        try {
            log.info("Generating potential cut off list report");
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_potential_cut_off");
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

                    List<PotentialCutOffRecord> records = new ArrayList<>();

                    for (Account acc : accounts) {

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
                            PotentialCutOffRecord pcr = new PotentialCutOffRecord();

                            if (acc.getConsumer() != null) {
                                pcr.setAccName(acc.getAccName());
                            }

                            if (acc.getZone() != null) {
                                pcr.setZone(acc.getZone().getName());
                            }


                            pcr.setAccNo(acc.getAccNo());

                            Page<Bill> bills;
                            bills = billRepository.findByAccountOrderByBillCodeDesc(acc, new PageRequest(0, 1));
                            if (bills.hasContent()) {
                                Bill lastBill = bills.getContent().get(0);

                                pcr.setLastBillingMonth(lastBill.getBillingMonth().getMonth());
                                pcr.setBeforeBilling(acc.getOutstandingBalance() - lastBill.getTotalBilled());

                                pcr.setBilledAmount(lastBill.getTotalBilled());
                            } else {
                                pcr.setBeforeBilling(0.0);
                            }
                            pcr.setAfterBilling(acc.getOutstandingBalance());


                            if (pcr.getBeforeBilling() > 0) {
                                records.add(pcr);
                            }
                        }
                    }
                    log.info("Packaged report data...");

                    ReportObject report = new ReportObject();
                    report.setDate(Calendar.getInstance());
                    accounts = null;
                    report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                    report.setTitle(this.optionService.getOption("REPORT:POTENTIAL_CUT_OFF").getValue());
                    report.setContent(records);
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
            ex.printStackTrace();
        }
        return response;
    }

    public RestResponse getBillingSummary(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_billing_summary");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                log.info("Getting billing summary report...");
                ReportsParam request = requestObject.getObject();

                Map<String, String> params = this.getParamsMap(request);

                BillingSummaryRecord bsr = new BillingSummaryRecord();


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

                    Double totalAmountBilled = 0.0;
                    Double totalMeterRent = 0.0;

                    for (Bill b : bills) {
                        Boolean include = true;
                        if (params.containsKey("zoneId")) {
                            Object zoneId = params.get("zoneId");
                            if (b.getAccount().getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                include = false;
                            }
                        }


                        if (include) {

                            totalAmountBilled += b.getAmount();
                            totalMeterRent += b.getMeterRent();
                            //amount billed
                            if (b.getConsumptionType().compareToIgnoreCase("Actual") == 0) {
                                bsr.setBilledOnActual(bsr.getBilledOnActual() + b.getAmount());
                            } else if (b.getConsumptionType().compareToIgnoreCase("Average") == 0) {
                                bsr.setBilledOnEstimate(bsr.getBilledOnEstimate() + b.getAmount());
                            }

                            //Other charges
                            if (!b.getBillItems().isEmpty()) {
                                for (BillItem bi : b.getBillItems()) {
                                    totalAmountBilled += bi.getAmount();
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
                    report.setAmount(totalAmountBilled);
                    report.setMeterRent(totalMeterRent);
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
            ex.printStackTrace();
        }
        return response;
    }

    public RestResponse getAccountStatement(RestRequestObject<ReportsParam> requestObject, Long accountId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_statement");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

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

                        //String billingYearMonth = billingMonth.get(Calendar.YEAR) + " " + billingMonth.get(Calendar.MONTH);

                        SimpleDateFormat format1 = new SimpleDateFormat("MMM, yyyy");
                        String formattedDate = format1.format(billingMonth.getTime());

                        StatementRecord billRecord = new StatementRecord();
                        billRecord.setTransactionDate(bill.getTransactionDate());
                        billRecord.setItemType("Bill");
                        billRecord.setRefNo(formattedDate);
                        billRecord.setAmount(bill.getAmount());
                        records.add(billRecord);

                        //get billing items
                        if (!bill.getBillItems().isEmpty()) {
                            for (BillItem billItem : bill.getBillItems()) {
                                StatementRecord billItemRecord = new StatementRecord();
                                billItemRecord.setTransactionDate(bill.getTransactionDate());
                                billItemRecord.setItemType("Charge");
                                billItemRecord.setRefNo(formattedDate);
                                billItemRecord.setAmount(billItem.getAmount());
                                records.add(billItemRecord);
                            }
                        }

                        //get meter rent
                        if (bill.getMeterRent() > 0) {
                            billRecord = new StatementRecord();
                            billRecord.setTransactionDate(bill.getTransactionDate());
                            billRecord.setItemType("Meter Rent");
                            billRecord.setRefNo(formattedDate);
                            billRecord.setAmount(bill.getMeterRent());
                            records.add(billRecord);
                        }
                    }
                }

                //add payments
                if (!account.getPayments().isEmpty()) {
                    for (Payment payment : account.getPayments()) {
                        StatementRecord paymentRecord = new StatementRecord();
                        paymentRecord.setTransactionDate(payment.getTransactionDate());

                        paymentRecord.setRefNo(payment.getReceiptNo() + "-" + payment.getPaymentType().getName());


                        //Double amount = Math.abs(payment.getAmount()) * -1;
                        Double amount = payment.getAmount();


                        if (payment.getPaymentType().isNegative()) {
                            amount = Math.abs(payment.getAmount());
                        } else {
                            if (amount > 0) {
                                amount = Math.abs(payment.getAmount()) * -1;
                            } else {
                                amount = Math.abs(payment.getAmount());
                            }
                        }

                        paymentRecord.setAmount(amount);
                        if (payment.getPaymentType().hasComments()) {
                            paymentRecord.setItemType("Adjustment");
                        } else {
                            paymentRecord.setItemType("Payment");
                        }

//                        if (!payment.getPaymentType().isNegative()) {
//                            paymentRecord.setAmount(amount * -1);
//
//                            if (payment.getPaymentType().getName().compareToIgnoreCase("Credit") == 0) {
//                                paymentRecord.setItemType("Adjustment");
//                            } else {
//                                paymentRecord.setItemType("Payment");
//                            }
//
//                            //paymentRecord.setItemType(payment.getPaymentType().getName());
//                        } else {
//                            //
//                            paymentRecord.setItemType("Adjustment");
//
//                        }

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
                response = authManager.grant(requestObject.getToken(), "report_payments");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
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
                        fromDate.setTimeInMillis(Long.valueOf(unixTime.toString()) * 1000);
                    }
                }

                //get payments to a specific date
                Calendar toDate = Calendar.getInstance();
                if (params.containsKey("toDate")) {
                    Object unixTime = params.get("toDate");
                    if (unixTime.toString().compareToIgnoreCase("null") != 0) {
                        Long milliSeconds = Long.valueOf(unixTime.toString()) * 1000;
                        toDate.setTimeInMillis(milliSeconds);
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

    public RestResponse getWaris(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_waris");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                log.info("Getting WARIS report...");
                ReportsParam request = requestObject.getObject();

                Map<String, String> params = this.getParamsMap(request);

                BillingSummaryRecord bsr = new BillingSummaryRecord();


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

                    Double totalAmountBilled = 0.0;
                    Double totalMeterRent = 0.0;

                    for (Bill b : bills) {
                        Boolean include = true;
                        if (params.containsKey("zoneId")) {
                            Object zoneId = params.get("zoneId");
                            if (b.getAccount().getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                include = false;
                            }
                        }


                        if (include) {

                            //accountRepository.fi
                            Long meterId = 0l;


                            Boolean isMetered = false;

                            try {
                                if (b.getAccount().isMetered()) {
                                    isMetered = true;
                                }
                            } catch (Exception ex) {
                                log.error(ex.getMessage());
                            }

                            totalAmountBilled += b.getAmount();
                            totalMeterRent += b.getMeterRent();
                            //amount billed
                            if (b.getConsumptionType().compareToIgnoreCase("Actual") == 0) {
                                //amount billed on actual
                                bsr.setBilledOnActual(bsr.getBilledOnActual() + b.getAmount());

                                //units billed on actual
                                bsr.setUnitsActualConsumption(bsr.getUnitsActualConsumption() + b.getUnitsBilled());
                                if (isMetered) {
                                    bsr.setMeteredBilledActual(bsr.getMeteredBilledActual() + 1);
                                }

                            } else if (b.getConsumptionType().compareToIgnoreCase("Average") == 0) {
                                //amount billed on average
                                bsr.setBilledOnEstimate(bsr.getBilledOnEstimate() + b.getAmount());

                                //units billed on average
                                bsr.setUnitsEstimatedConsumption(bsr.getUnitsEstimatedConsumption() + b.getAverageConsumption());
                                if (isMetered) {
                                    bsr.setMeteredBilledAverage(bsr.getMeteredBilledAverage() + 1);
                                }
                            }

                            //Other charges
                            if (!b.getBillItems().isEmpty()) {
                                for (BillItem bi : b.getBillItems()) {
                                    totalAmountBilled += bi.getAmount();
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
                                } else {
                                    //total payments
                                    bsr.setTotalPayments(bsr.getTotalPayments() + p.getAmount());
                                }
                            }
                        }
                    }

                    //bsr.set

                    bsr.setActiveAccounts(Integer.valueOf(accountRepository.countByActive(true).toString()));
                    bsr.setInactiveAccounts(Integer.valueOf(accountRepository.countByActive(false).toString()));

                    bsr.setBalancesActiveAccounts(accountRepository.getOutstandingBalancesByStatus(1));
                    bsr.setBalancesInactiveAccounts(accountRepository.getOutstandingBalancesByStatus(0));


                    bsr.setActiveMeteredAccounts(accountRepository.getActiveMeteredAccounts());
                    bsr.setActiveUnMeteredAccounts(accountRepository.getActiveUnMeteredAccounts());


                    //send report
                    ReportObject report = new ReportObject();
                    report.setDate(Calendar.getInstance());
                    report.setAmount(totalAmountBilled);
                    report.setMeterRent(totalMeterRent);
                    report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                    report.setTitle(this.optionService.getOption("REPORT:WARIS").getValue());
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
            ex.printStackTrace();
        }
        return response;
    }

}
