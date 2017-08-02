package ke.co.suncha.simba.aqua.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.admin.repositories.UserRepository;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.account.QAccount;
import ke.co.suncha.simba.aqua.account.scheme.Scheme;
import ke.co.suncha.simba.aqua.account.scheme.SchemeRepository;
import ke.co.suncha.simba.aqua.billing.Bill;
import ke.co.suncha.simba.aqua.billing.BillService;
import ke.co.suncha.simba.aqua.billing.QBill;
import ke.co.suncha.simba.aqua.billing.TransferredBill;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.reports.*;
import ke.co.suncha.simba.aqua.reports.scheduled.QAccountBalanceRecord;
import ke.co.suncha.simba.aqua.repository.*;
import ke.co.suncha.simba.aqua.toActivate.QToActivate;
import ke.co.suncha.simba.aqua.toActivate.ToActivateRecord;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 5/6/15.
 */
@Service
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
    AgeingDataRepository ageingDataRepository;

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

    @Autowired
    PaymentTypeRepository paymentTypeRepository;

    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SchemeRepository schemeRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    BillItemRepository billItemRepository;


    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public ReportService() {
    }

    private Integer getYearMonthDayFromCalendar(DateTime dateTime) {
        Integer year = dateTime.getYear();
        Integer month = dateTime.getMonthOfYear() + 1;
        Integer day = dateTime.getDayOfMonth();

        String content = year.toString();

        if (month.toString().length() == 1) {
            content = content + "0" + month.toString();
        } else {
            content = content + month.toString();
        }

        if (day.toString().length() == 1) {
            content = content + "0" + day.toString();
        } else {
            content = content + day.toString();
        }

        Integer val = Integer.valueOf(content);

        return val;
    }

    private List<String> getContentMeta(String value) {
        String[] billContent = value.split("#");
        List<String> list = new ArrayList<>();
        if (billContent.length > 0) {
            for (String s : billContent) {
                if (s.length() > 0) {
                    list.add(s);
                }
            }
        }
        return list;
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

                //SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                log.info("Getting bills for billing month:" + billingMonth.getMonth().toString("yyyy-MM-dd"));

                DateTime bMonth = billingMonth.getMonth();

                //get bill id's belonging to billing month
                List<Long> billIds = new ArrayList<>();

                //Bills builder
                BooleanBuilder billsBuilder = new BooleanBuilder();
                billsBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
                billsBuilder.and(QBill.bill.transferred.eq(Boolean.FALSE));

                //Zone param
                if (params.containsKey("zoneId")) {
                    Object zoneId = params.get("zoneId");
                    billsBuilder.and(QBill.bill.account.zone.zoneId.eq(Long.valueOf(zoneId.toString())));
                }


                if (params.containsKey("accNo")) {
                    //Get account number from params
                    String accNo = params.get("accNo").toString();
                    if (!accNo.isEmpty() && accNo != null) {
                        Account account = accountRepository.findByaccNo(accNo);
                        if (account != null) {
                            billsBuilder.and(QBill.bill.account.accountId.eq(account.getAccountId()));
                        }
                    }
                }

                JPAQuery query = new JPAQuery(entityManager);
                billIds = query.from(QBill.bill).where(billsBuilder).list(QBill.bill.billId);
                if (billIds == null) {
                    billIds = new ArrayList<>();
                }

                log.info("Bills " + billIds.size() + " found.");
                if (billIds == null || billIds.isEmpty()) {
                    responseObject.setMessage("No bills found");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                List<MonthlyBillRecord> records = new ArrayList<>();
                Integer counter = 0;
                for (Long billId : billIds) {
                    Bill b = billRepository.findOne(billId);

                    counter++;
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
                    monthlyBillRecord.setBillingMonth(billingMonth.getMonth());
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


                    //get payments from the previous month
                    //List<Payment> payments = paymentRepository.findByBillingMonth_BillingMonthIdAndAccount(paymentBillingMonth.getBillingMonthId(), b.getAccount());
                    //List<Payment> payments = paymentRepository.findByTransactionDateBetweenAndAccount(startDate, toDate, b.getAccount());
                    Long paymentAccountId = b.getAccount().getAccountId();


                    Long accountId = billRepository.findAccountIdByBillId(b.getBillId());

                    DateTime previousBillingDate = new DateTime().dayOfMonth().withMinimumValue().hourOfDay().withMinimumValue();

                    Integer previousBillCode = billRepository.findPreviousBillCode(accountId, b.getBillCode());
                    Boolean hadAPreviousBill = Boolean.FALSE;

                    if (previousBillCode != null) {
                        //get the actual previous bill
                        // Calendar c = Calendar.getInstance();
//                            log.info("account id:" + accountId);
//                            log.info("bill code:" + billCode);
                        Timestamp timestamp = billRepository.findPreviousBillDate(accountId, previousBillCode);
                        if (timestamp != null) {
                            previousBillingDate = previousBillingDate.withMillis(timestamp.getTime());
                            hadAPreviousBill = Boolean.TRUE;
                        }
                    }

                    log.info("Previous bill code:" + previousBillCode);
                    log.info("Previous billing date: " + previousBillingDate);

                    //Double balanceBeforeBill = accountService.getAccountBalanceByTransDate(b.getAccount().getAccountId(), lastDateBeforeBillingCycle);
                    Double balanceBeforeBill = accountService.getAccountBalanceByTransDate(b.getAccount().getAccountId(), previousBillingDate.plusSeconds(1));

                    log.info("Balance before bill:" + balanceBeforeBill);


                    //region Get transferred bills
                    Double otherBillsTotal = 0.0;
                    BooleanBuilder transferredBillIdsBuilder = new BooleanBuilder();
                    transferredBillIdsBuilder.and(QBill.bill.account.accountId.eq(accountId));
                    transferredBillIdsBuilder.and(QBill.bill.transactionDate.gt(previousBillingDate));
                    transferredBillIdsBuilder.and(QBill.bill.transactionDate.loe(b.getTransactionDate()));
                    transferredBillIdsBuilder.and(QBill.bill.transferred.eq(Boolean.TRUE));

                    List<Long> transferredBillIds = new ArrayList<>();
                    JPAQuery billIdsQuery = new JPAQuery(entityManager);
                    transferredBillIds = billIdsQuery.from(QBill.bill).where(transferredBillIdsBuilder).list(QBill.bill.billId);

                    if (transferredBillIds == null) {
                        transferredBillIds = new ArrayList<>();
                    }

                    if (!transferredBillIds.isEmpty()) {
                        List<TransferredBill> transferredBills = new ArrayList<>();
                        for (Long transferredBillId : transferredBillIds) {
                            Bill transferredBill = billService.getById(transferredBillId);
                            TransferredBill tb = new TransferredBill();
                            tb.setTransactionDate(transferredBill.getTransactionDate());
                            tb.setUnits(transferredBill.getUnitsBilled());
                            tb.setAmount(transferredBill.getTotalBilled());
                            tb.setContent(this.getContentMeta(transferredBill.getContent()));

                            otherBillsTotal += transferredBill.getTotalBilled();

                            //add to list
                            transferredBills.add(tb);
                        }

                        if (!transferredBills.isEmpty()) {
                            monthlyBillRecord.setHasOtherBills(Boolean.TRUE);
                            monthlyBillRecord.setBills(transferredBills);
                        }
                    }

                    monthlyBillRecord.setOtherBillsTotal(otherBillsTotal);

                    //endregion


                    //Integer intBillCode = Integer.valueOf(lastBillingDate.get(Calendar.YEAR) + "" + lastBillingDate.get(Calendar.MONTH) + "" + lastBillingDate.get(Calendar.DAY_OF_MONTH));
                    Integer intBillCode = this.getYearMonthDayFromCalendar(previousBillingDate);

                    Double paymentsDoneOnBillingDay = 0.0;

                    BooleanBuilder paymentsBuilder = new BooleanBuilder();
                    paymentsBuilder.and(QPayment.payment.account.accountId.eq(accountId));
                    paymentsBuilder.and(QPayment.payment.transactionDate.gt(previousBillingDate));
                    paymentsBuilder.and(QPayment.payment.transactionDate.loe(b.getTransactionDate()));

                    JPAQuery paymentsQuery = new JPAQuery(entityManager);

                    List<Payment> payments = paymentsQuery.from(QPayment.payment).where(paymentsBuilder).list(QPayment.payment);
                    if (payments == null) {
                        payments = new ArrayList<>();
                    }

                    if (!payments.isEmpty()) {
                        //log.info("Payments: " + payments.size());
                        List<PaymentRecord> paymentRecords = new ArrayList<>();

                        for (Payment p : payments) {


                            //Integer intPaymentCode = Integer.valueOf(p.getTransactionDate().get(Calendar.YEAR) + "" + p.getTransactionDate().get(Calendar.MONTH) + "" + p.getTransactionDate().get(Calendar.DAY_OF_MONTH));
                            Integer intPaymentCode = this.getYearMonthDayFromCalendar(p.getTransactionDate());

                            //log.info("intPaymentCode:" + intPaymentCode);
                            //log.info("intBillCode:" + intBillCode);
                            //log.info("Payment amount:" + p.getAmount());
                            if (intPaymentCode > intBillCode) {
                                totalPayments += p.getAmount();
                                PaymentRecord paymentRecord = new PaymentRecord();
                                paymentRecord.setTransactionDate(p.getTransactionDate());
                                paymentRecord.setAmount(p.getAmount());
                                if (!p.getPaymentType().getName().equals("Water Sale")) {
                                    paymentRecord.setReceiptNo(p.getReceiptNo() + "(" + p.getPaymentType().getName() + ")");
                                } else {
                                    paymentRecord.setReceiptNo(p.getReceiptNo());
                                }

                                paymentRecords.add(paymentRecord);

                                //
                                //log.info("PAYMENT CODE:" + intPaymentCode);
                                //log.info("BILL CODE:" + intBillCode);
                                if (intBillCode.compareTo(intPaymentCode) == 0) {
                                    paymentsDoneOnBillingDay += p.getAmount();
                                    //log.info("Adding payments done on same day...");
                                }
                            }
                        }
                        monthlyBillRecord.setPayments(paymentRecords);

                    }
                    monthlyBillRecord.setTotalPayments(totalPayments);

                    //set balance befor bill
                    //log.info("Balance b4 bill:" + balanceBeforeBill);
                    //log.info("Payments done on billing day:" + paymentsDoneOnBillingDay);
                    balanceBeforeBill = balanceBeforeBill + paymentsDoneOnBillingDay;


                    //less b/f transferred bills
                    if (hadAPreviousBill) {
                        balanceBeforeBill = balanceBeforeBill - otherBillsTotal;
                    }


                    Double paymentsOnBill = monthlyBillRecord.getTotalPayments();


                    monthlyBillRecord.setBalanceBf(balanceBeforeBill);

                    //check if in arreas
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
                report.setImageUrl(this.optionService.getOption("MONTHLY_WATER_BILL:IMAGE_URL").getValue());
                report.setAddress(this.optionService.getOption("MONTHLY_WATER_BILL:ADDRESS").getValue());

                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(report);
                response = new RestResponse(responseObject, HttpStatus.OK);

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


                List<BigInteger> accountList = accountRepository.findAllAccountIds();
                if (!accountList.isEmpty()) {
                    log.info(accountList.size() + " accounts found.");

                    List<PotentialCutOffRecord> records = new ArrayList<>();

                    for (BigInteger accId : accountList) {
                        Account acc = accountRepository.findOne(accId.longValue());
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
//                                if (params.containsKey("accountStatus")) {
//                                    String status = params.get("accountStatus");
//                                    if (status.compareToIgnoreCase("inactive") == 0) {
//                                        if (acc.isActive()) {
//                                            include = false;
//                                        }
//                                    } else if (status.compareToIgnoreCase("active") == 0) {
//                                        if (!acc.isActive()) {
//                                            include = false;
//                                        }
//                                    }
//                                }
                            }
                        }
                        if (!acc.isActive()) {
                            include = false;
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

    public RestResponse getAccountsNotBilled(RestRequestObject<ReportsParam> requestObject) {
        try {
            log.info("Generating accounts not billed report");
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_accounts_not_billed");
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
                BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);

                if (billingMonth == null) {
                    responseObject.setMessage("You do not have open billing month.");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                    return response;
                }

                List<BigInteger> accountList = accountRepository.findAllAccountIds();
                if (!accountList.isEmpty()) {
                    log.info(accountList.size() + " accounts found.");

                    List<PotentialCutOffRecord> records = new ArrayList<>();

                    for (BigInteger accId : accountList) {
                        Account acc = accountRepository.findOne(accId.longValue());
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
                            }
                        }
                        //account status
                        if (!acc.isActive()) {
                            include = false;
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
                            List<BigInteger> bills = billRepository.findAllByBillingMonthAndAccount_AccNo(billingMonth.getBillingMonthId(), accId.longValue());
                            if (bills.isEmpty()) {
                                records.add(pcr);
                            }
                        }
                    }
                    log.info("Packaged report data...");

                    ReportObject report = new ReportObject();
                    report.setDate(Calendar.getInstance());
                    report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                    report.setTitle(this.optionService.getOption("REPORT:ACCOUNTS_NOT_BILLED").getValue());
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


                List<BigInteger> bills;
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

                    bills = billRepository.findAllByBillingMonth(billingMonth.getBillingMonthId());
                    log.info("Bills " + bills.size() + " found.");
                    if (bills == null || bills.isEmpty()) {
                        responseObject.setMessage("No content found");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }

                    Double totalAmountBilled = 0.0;
                    Double totalMeterRent = 0.0;

                    for (BigInteger billId : bills) {
                        Bill b = billRepository.findOne(billId.longValue());
                        Boolean include = true;
                        if (params.containsKey("zoneId")) {
                            Object zoneId = params.get("zoneId");
                            if (b.getAccount().getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                include = false;
                            }
                        }


                        if (include) {

                            totalAmountBilled += b.getAmount();
                            totalAmountBilled += b.getMeterRent();
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

                    //Get
                    Long zoneId = 0L;
                    if (params.containsKey("zoneId")) {
                        Object zoneObj = params.get("zoneId");
                        zoneId = Long.valueOf(zoneObj.toString());
                    }

                    PaymentType credit = paymentTypeRepository.findByName("CREDIT");
                    PaymentType debit = paymentTypeRepository.findByName("DEBIT");
                    Double creditAdjustmentTotal = 0d;
                    Double debitAdjustmentTotal = 0d;
                    DateTime fromDate = new DateTime().withMillis(billingMonth.getMonth().getMillis()).withTimeAtStartOfDay().withDayOfMonth(1);
                    DateTime toDate = fromDate.plusMonths(1).minusMillis(1);

                    if (zoneId > 0) {

                    } else {
                        creditAdjustmentTotal = paymentRepository.getTotalByPaymentTypeByDate(credit.getPaymentTypeId(), fromDate.toString("yyyy-MM-dd"), toDate.toString("yyyy-MM-dd"));
                        debitAdjustmentTotal = paymentRepository.getTotalByPaymentTypeByDate(debit.getPaymentTypeId(), fromDate.toString("yyyy-MM-dd"), toDate.toString("yyyy-MM-dd"));
                    }
                    bsr.setCreditAdjustments(creditAdjustmentTotal);
                    bsr.setDebitAdjustments(debitAdjustmentTotal);

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
                        DateTime billingMonth = bill.getBillingMonth().getMonth();
                        String formattedDate = billingMonth.toString("MMM, yyyy");
                        //format1.format(billingMonth.getTime());

                        StatementRecord billRecord = new StatementRecord();
                        //billRecord.setTransactionDate(bill.getTransactionDate());
                        billRecord.setTransactionDate(bill.getBillingMonth().getMonth());
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

    public RestResponse getPayments(RestRequestObject<AccountsReportRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_payments");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                log.info("Getting payments report...");


                AccountsReportRequest reportRequest = requestObject.getObject();


//                BillingMonth billingMonth = billingMonthRepository.findOne(accountsReportRequest.getBillingMonthId());
//                if (billingMonth == null) {
//                    responseObject.setMessage("Invalid billing month");
//                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
//                    return response;
//                }

                BooleanBuilder builder = new BooleanBuilder();


                //Zone
                if (reportRequest.getZoneId() != null) {
                    builder.and(QPayment.payment.account.zone.zoneId.eq(reportRequest.getZoneId()));
                } else {
                    //Scheme
                    if (reportRequest.getSchemeId() != null) {
                        List<BigInteger> zoneIDs = zoneRepository.findAllBySchemeId(reportRequest.getSchemeId());
                        if (!zoneIDs.isEmpty()) {
                            BooleanBuilder zoneBuilder = new BooleanBuilder();
                            for (BigInteger zoneID : zoneIDs) {
                                zoneBuilder.or(QPayment.payment.account.zone.zoneId.eq(zoneID.longValue()));
                            }
                            builder.and(zoneBuilder);
                        }
                    }
                }

                //Payment type
                Boolean isAdjustment = Boolean.FALSE;
                if (reportRequest.getPaymentTypeId() != null) {
                    PaymentType paymentType = paymentTypeRepository.findOne(reportRequest.getPaymentTypeId());
                    if (paymentType != null) {
                        if (StringUtils.equalsIgnoreCase(paymentType.getName(), "Debit") || StringUtils.equalsIgnoreCase(paymentType.getName(), "Credit")) {
                            isAdjustment = Boolean.TRUE;
                        }
                    }
                    builder.and(QPayment.payment.paymentType.paymentTypeId.eq(reportRequest.getPaymentTypeId()));
                }

                //Payment source
                if (reportRequest.getPaymentSourceId() != null) {
                    if (!isAdjustment) {
                        builder.and(QPayment.payment.paymentSource.paymentSourceId.eq(reportRequest.getPaymentSourceId()));
                    }
                }

                if (reportRequest.getFromDate() == null) {
                    responseObject.setMessage("From date can not be empty.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }


                if (reportRequest.getToDate() == null) {
                    responseObject.setMessage("To date can not be empty.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                // DateTime test= reportRequest.getFromDate().withZone(DateTimeZone.forID("Africa/Nairobi"));

                reportRequest.setFromDate(reportRequest.getFromDate().withZone(DateTimeZone.forID("Africa/Nairobi")));
                reportRequest.setToDate(reportRequest.getToDate().withZone(DateTimeZone.forID("Africa/Nairobi")));

                if (reportRequest.getFromDate().isAfter(reportRequest.getToDate())) {
                    responseObject.setMessage("From date can not be after to date.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }


                DateTime fromDate = new DateTime();
                DateTime toDate = new DateTime();
                BooleanBuilder dateBuilder = new BooleanBuilder();
                if (reportRequest.getFromDate().isEqual(reportRequest.getToDate())) {
                    fromDate = fromDate.withMillis(reportRequest.getFromDate().hourOfDay().withMinimumValue().getMillis());
                    toDate = toDate.withMillis(reportRequest.getFromDate().hourOfDay().withMaximumValue().getMillis());
                    dateBuilder.and(QPayment.payment.transactionDate.between(fromDate, toDate));
                    //dateBuilder.and(QPayment.payment.transactionDate.loe(toDate));
                } else {
                    fromDate = fromDate.withMillis(reportRequest.getFromDate().hourOfDay().withMinimumValue().getMillis());
                    toDate = toDate.withMillis(reportRequest.getToDate().hourOfDay().withMaximumValue().getMillis());

                    dateBuilder.and(QPayment.payment.transactionDate.between(fromDate, toDate));
                    //dateBuilder.and(QPayment.payment.transactionDate.loe(toDate));
                }
                builder.and(dateBuilder);


                Iterable<Payment> payments = paymentRepository.findAll(builder, QPayment.payment.transactionDate.desc());
                Double totalAmount = 0.0;
                List<PaymentRecord> records = new ArrayList<>();
                for (Payment p : payments) {
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

    public RestResponse getWaris(RestRequestObject<AccountsReportRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_waris");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                log.info("Getting WARIS report...");

                AccountsReportRequest accountsReportRequest = requestObject.getObject();
                if (accountsReportRequest.getBillingMonthId() == null) {
                    responseObject.setMessage("Billing month can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                BillingMonth billingMonth = billingMonthRepository.findOne(accountsReportRequest.getBillingMonthId());
                if (billingMonth == null) {
                    responseObject.setMessage("Invalid billing month resource");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

//                if (accountsReportRequest.getSchemeId() == null) {
//                    responseObject.setMessage("Scheme can not be empty");
//                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
//                    return response;
//                }
//
//                Scheme scheme = schemeRepository.findOne(accountsReportRequest.getSchemeId());
//                if (scheme == null) {
//                    responseObject.setMessage("Invalid scheme resource");
//                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
//                    return response;
//                }

                BillingSummaryRecord bsr = new BillingSummaryRecord();


                JPAQuery query = new JPAQuery(entityManager);
                BooleanBuilder billsBuilder = new BooleanBuilder();
                billsBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));

                BooleanBuilder zoneBillsBuilder = new BooleanBuilder();

                if (accountsReportRequest.getZoneId() != null) {
                    zoneBillsBuilder.and(QBill.bill.account.zone.zoneId.eq(accountsReportRequest.getZoneId()));
                } else if (accountsReportRequest.getSchemeId() != null) {
                    Scheme scheme = schemeRepository.findOne(accountsReportRequest.getSchemeId());
                    if (scheme != null) {
                        List<BigInteger> zoneIDs = zoneRepository.findAllBySchemeId(scheme.getSchemeId());
                        if (!zoneIDs.isEmpty()) {
                            for (BigInteger zoneID : zoneIDs) {
                                zoneBillsBuilder.or(QBill.bill.account.zone.zoneId.eq(zoneID.longValue()));
                            }
                        }
                    }
                }

                billsBuilder.and(zoneBillsBuilder);

                List<Long> bills = query.from(QBill.bill).where(billsBuilder).list(QBill.bill.billId);
                log.info("Bills " + bills.size() + " found.");
                if (bills == null || bills.isEmpty()) {
                    responseObject.setMessage("No content found");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                Double totalAmountBilled = 0.0;


                //Get total amount billed on actual
                query = new JPAQuery(entityManager);
                BooleanBuilder billedOnActualBuilder = new BooleanBuilder();
                billedOnActualBuilder.and(QBill.bill.consumptionType.equalsIgnoreCase("Actual"));
                billedOnActualBuilder.and(zoneBillsBuilder);
                billedOnActualBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
                Double billedOnActualTotal = query.from(QBill.bill).where(billedOnActualBuilder).singleResult(QBill.bill.amount.sum());
                bsr.setBilledOnActual(billedOnActualTotal);
                totalAmountBilled += billedOnActualTotal;

                //Get total Billed on average
                query = new JPAQuery(entityManager);
                BooleanBuilder billedOnAverageBuilder = new BooleanBuilder();
                billedOnAverageBuilder.and(QBill.bill.consumptionType.equalsIgnoreCase("Average"));
                billedOnAverageBuilder.and(zoneBillsBuilder);
                billedOnAverageBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
                Double billedOnAverageTotal = query.from(QBill.bill).where(billedOnAverageBuilder).singleResult(QBill.bill.amount.sum());
                bsr.setBilledOnEstimate(billedOnAverageTotal);
                totalAmountBilled += billedOnAverageTotal;

                //Get metered billed on actual
                query = new JPAQuery(entityManager);
                BooleanBuilder meteredOnActualBuilder = new BooleanBuilder();
                meteredOnActualBuilder.and(QBill.bill.consumptionType.equalsIgnoreCase("Actual"));
                meteredOnActualBuilder.and(zoneBillsBuilder);
                meteredOnActualBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
                meteredOnActualBuilder.and(QBill.bill.account.meter.isNotNull());
                Long meteredBilledOnActual = query.from(QBill.bill).where(meteredOnActualBuilder).singleResult(QBill.bill.count());

                //Get metered billed on actual
                query = new JPAQuery(entityManager);
                BooleanBuilder meteredOnAverageBuilder = new BooleanBuilder();
                meteredOnAverageBuilder.and(QBill.bill.consumptionType.equalsIgnoreCase("Average"));
                meteredOnAverageBuilder.and(zoneBillsBuilder);
                meteredOnAverageBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
                meteredOnAverageBuilder.and(QBill.bill.account.meter.isNotNull());
                Long meteredBilledOnAverage = query.from(QBill.bill).where(meteredOnAverageBuilder).singleResult(QBill.bill.count());


                //Get units billed on actual
                query = new JPAQuery(entityManager);
                BooleanBuilder unitsOnActualBuilder = new BooleanBuilder();
                unitsOnActualBuilder.and(QBill.bill.consumptionType.equalsIgnoreCase("Actual"));
                unitsOnActualBuilder.and(zoneBillsBuilder);
                unitsOnActualBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
                Double unitsBilledOnActual = query.from(QBill.bill).where(unitsOnActualBuilder).singleResult(QBill.bill.unitsBilled.sum());
                bsr.setUnitsActualConsumption(unitsBilledOnActual);

                //Get units billed on average
                query = new JPAQuery(entityManager);
                BooleanBuilder unitsOnAverageBuilder = new BooleanBuilder();
                unitsOnAverageBuilder.and(QBill.bill.consumptionType.equalsIgnoreCase("Average"));
                unitsOnAverageBuilder.and(zoneBillsBuilder);
                unitsOnAverageBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
                Double unitsOnAverage = query.from(QBill.bill).where(unitsOnAverageBuilder).singleResult(QBill.bill.unitsBilled.sum());
                bsr.setUnitsEstimatedConsumption(unitsOnAverage);


                //Get total Meter Rent
                query = new JPAQuery(entityManager);
                BooleanBuilder meterRentBuilder = new BooleanBuilder();
                meterRentBuilder.and(zoneBillsBuilder);
                meterRentBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
                Double meterRentTotal = query.from(QBill.bill).where(meterRentBuilder).singleResult(QBill.bill.meterRent.sum());
                totalAmountBilled += meterRentTotal;

                //Get Other charges

                //Reconnection fee
                query = new JPAQuery(entityManager);
                BooleanBuilder reconnectionFeeBuilder = new BooleanBuilder();
                reconnectionFeeBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("Reconnection Fee").getBillTypeId()));
                reconnectionFeeBuilder.and(QBillItem.billItem.bill.billId.in(bills));
                Double reconnectionFeeTotal = query.from(QBillItem.billItem).where(reconnectionFeeBuilder).singleResult(QBillItem.billItem.amount.sum());
                if (reconnectionFeeTotal == null) reconnectionFeeTotal = 0d;
                bsr.setReconnectionFee(reconnectionFeeTotal);
                totalAmountBilled += reconnectionFeeTotal;

                //At Owners Request Fee
                query = new JPAQuery(entityManager);
                BooleanBuilder ownersRequestFeeBuilder = new BooleanBuilder();
                ownersRequestFeeBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("At Owners Request Fee").getBillTypeId()));
                ownersRequestFeeBuilder.and(QBillItem.billItem.bill.billId.in(bills));
                Double ownersRequestFeeTotal = query.from(QBillItem.billItem).where(ownersRequestFeeBuilder).singleResult(QBillItem.billItem.amount.sum());
                if (ownersRequestFeeTotal == null) ownersRequestFeeTotal = 0d;
                bsr.setAtOwnersRequestFee(ownersRequestFeeTotal);
                totalAmountBilled += ownersRequestFeeTotal;

                //Change Of Account Name
                query = new JPAQuery(entityManager);
                BooleanBuilder changeOfAccountBuilder = new BooleanBuilder();
                changeOfAccountBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("Change Of Account Name").getBillTypeId()));
                changeOfAccountBuilder.and(QBillItem.billItem.bill.billId.in(bills));
                Double changeOfAccountFeeTotal = query.from(QBillItem.billItem).where(changeOfAccountBuilder).singleResult(QBillItem.billItem.amount.sum());
                if (changeOfAccountFeeTotal == null) changeOfAccountFeeTotal = 0d;
                bsr.setChangeOfAccountName(changeOfAccountFeeTotal);
                totalAmountBilled += changeOfAccountFeeTotal;

                //By Pass Fee
                query = new JPAQuery(entityManager);
                BooleanBuilder byPassFeeBuilder = new BooleanBuilder();
                byPassFeeBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("By Pass Fee").getBillTypeId()));
                byPassFeeBuilder.and(QBillItem.billItem.bill.billId.in(bills));
                Double byPassFeeTotal = query.from(QBillItem.billItem).where(byPassFeeBuilder).singleResult(QBillItem.billItem.amount.sum());
                if (byPassFeeTotal == null) byPassFeeTotal = 0d;
                bsr.setByPassFee(byPassFeeTotal);
                totalAmountBilled += byPassFeeTotal;

                //Bounced Cheque Fee
                query = new JPAQuery(entityManager);
                BooleanBuilder bouncedChequeFeeBuilder = new BooleanBuilder();
                bouncedChequeFeeBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("Bounced Cheque Fee").getBillTypeId()));
                bouncedChequeFeeBuilder.and(QBillItem.billItem.bill.billId.in(bills));
                Double bouncedChequeFeeTotal = query.from(QBillItem.billItem).where(bouncedChequeFeeBuilder).singleResult(QBillItem.billItem.amount.sum());
                if (bouncedChequeFeeTotal == null) bouncedChequeFeeTotal = 0d;
                bsr.setBouncedChequeFee(bouncedChequeFeeTotal);
                totalAmountBilled += bouncedChequeFeeTotal;

                //Surcharge Irrigation
                query = new JPAQuery(entityManager);
                BooleanBuilder surchageIrrigationFeeBuilder = new BooleanBuilder();
                surchageIrrigationFeeBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("Surcharge Irrigation").getBillTypeId()));
                surchageIrrigationFeeBuilder.and(QBillItem.billItem.bill.billId.in(bills));
                Double surchageIrrigationFeeTotal = query.from(QBillItem.billItem).where(surchageIrrigationFeeBuilder).singleResult(QBillItem.billItem.amount.sum());
                if (surchageIrrigationFeeTotal == null) surchageIrrigationFeeTotal = 0d;
                bsr.setSurchargeIrrigation(surchageIrrigationFeeTotal);
                totalAmountBilled += surchageIrrigationFeeTotal;

                //Surcharge Missuse
                query = new JPAQuery(entityManager);
                BooleanBuilder surchageMissuseFeeBuilder = new BooleanBuilder();
                surchageMissuseFeeBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("Surcharge Missuse").getBillTypeId()));
                surchageMissuseFeeBuilder.and(QBillItem.billItem.bill.billId.in(bills));
                Double surchageMissuseFeeTotal = query.from(QBillItem.billItem).where(surchageMissuseFeeBuilder).singleResult(QBillItem.billItem.amount.sum());
                if (surchageMissuseFeeTotal == null) surchageMissuseFeeTotal = 0d;
                bsr.setSurchargeMissuse(surchageMissuseFeeTotal);
                totalAmountBilled += surchageMissuseFeeTotal;

                //Meter Servicing
                query = new JPAQuery(entityManager);
                BooleanBuilder meterServicingFeeBuilder = new BooleanBuilder();
                meterServicingFeeBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("Meter Servicing").getBillTypeId()));
                meterServicingFeeBuilder.and(QBillItem.billItem.bill.billId.in(bills));
                Double meterServicingFeeTotal = query.from(QBillItem.billItem).where(meterServicingFeeBuilder).singleResult(QBillItem.billItem.amount.sum());
                if (meterServicingFeeTotal == null) meterServicingFeeTotal = 0d;
                bsr.setMeterServicing(meterServicingFeeTotal);
                totalAmountBilled += meterServicingFeeTotal;


//                for (Long billId : bills) {
//                    Bill b = billRepository.findOne(billId);
//                    //accountRepository.fi
//                    Long meterId = 0l;
//                    Boolean isMetered = false;
//
//                    try {
//                        if (b.getAccount().isMetered()) {
//                            isMetered = true;
//                        }
//                    } catch (Exception ex) {
//                        log.error(ex.getMessage());
//                    }
//
//                    totalAmountBilled += b.getAmount();
//                    totalMeterRent += b.getMeterRent();
//                    //amount billed
//                    if (b.getConsumptionType().compareToIgnoreCase("Actual") == 0) {
//                        //amount billed on actual
//                        bsr.setBilledOnActual(bsr.getBilledOnActual() + b.getAmount());
//
//                        //units billed on actual
//                        bsr.setUnitsActualConsumption(bsr.getUnitsActualConsumption() + b.getUnitsBilled());
//                        if (isMetered) {
//                            bsr.setMeteredBilledActual(bsr.getMeteredBilledActual() + 1);
//                        }
//
//                    } else if (b.getConsumptionType().compareToIgnoreCase("Average") == 0) {
//                        //amount billed on average
//                        bsr.setBilledOnEstimate(bsr.getBilledOnEstimate() + b.getAmount());
//
//                        //units billed on average
//                        bsr.setUnitsEstimatedConsumption(bsr.getUnitsEstimatedConsumption() + b.getAverageConsumption());
//                        if (isMetered) {
//                            bsr.setMeteredBilledAverage(bsr.getMeteredBilledAverage() + 1);
//                        }
//                    }
//
//                    //Other charges
//                    if (!b.getBillItems().isEmpty()) {
//                        for (BillItem bi : b.getBillItems()) {
//                            totalAmountBilled += bi.getAmount();
//                            if (bi.getBillItemType().getName().compareToIgnoreCase("Reconnection Fee") == 0) {
//                                bsr.setReconnectionFee(bsr.getReconnectionFee() + bi.getAmount());
//                            } else if (bi.getBillItemType().getName().compareToIgnoreCase("At Owners Request Fee") == 0) {
//                                bsr.setAtOwnersRequestFee(bsr.getAtOwnersRequestFee() + bi.getAmount());
//                            } else if (bi.getBillItemType().getName().compareToIgnoreCase("Change Of Account Name") == 0) {
//                                bsr.setChangeOfAccountName(bsr.getChangeOfAccountName() + bi.getAmount());
//                            } else if (bi.getBillItemType().getName().compareToIgnoreCase("By Pass Fee") == 0) {
//                                bsr.setByPassFee(bsr.getByPassFee() + bi.getAmount());
//                            } else if (bi.getBillItemType().getName().compareToIgnoreCase("Bounced Cheque Fee") == 0) {
//                                bsr.setBouncedChequeFee(bsr.getBouncedChequeFee() + bi.getAmount());
//                            } else if (bi.getBillItemType().getName().compareToIgnoreCase("Surcharge Irrigation") == 0) {
//                                bsr.setSurchargeIrrigation(bsr.getSurchargeIrrigation() + bi.getAmount());
//                            } else if (bi.getBillItemType().getName().compareToIgnoreCase("Surcharge Missuse") == 0) {
//                                bsr.setSurchargeMissuse(bsr.getSurchargeMissuse() + bi.getAmount());
//                            } else if (bi.getBillItemType().getName().compareToIgnoreCase("Meter Servicing") == 0) {
//                                bsr.setMeterServicing(bsr.getMeterServicing() + bi.getAmount());
//                            }
//                        }
//                    }
//
//                }

                PaymentType credit = paymentTypeRepository.findByName("CREDIT");
                PaymentType debit = paymentTypeRepository.findByName("DEBIT");
                Double creditAdjustmentTotal = 0d;
                Double debitAdjustmentTotal = 0d;
                DateTime fromDate = new DateTime().withMillis(billingMonth.getMonth().getMillis()).withTimeAtStartOfDay().dayOfMonth().withMinimumValue();
                DateTime toDate = fromDate.dayOfMonth().withMaximumValue();

                DateTime from = fromDate;
                DateTime to = toDate;

                BooleanBuilder zonePaymentsBuilder = new BooleanBuilder();
                if (accountsReportRequest.getZoneId() != null) {
                    zonePaymentsBuilder.and(QPayment.payment.account.zone.zoneId.eq(accountsReportRequest.getZoneId()));
                } else if (accountsReportRequest.getSchemeId() != null) {
                    Scheme scheme = schemeRepository.findOne(accountsReportRequest.getSchemeId());
                    if (scheme != null) {
                        List<BigInteger> zoneIDs = zoneRepository.findAllBySchemeId(scheme.getSchemeId());
                        if (!zoneIDs.isEmpty()) {
                            BooleanBuilder zoneBuilder = new BooleanBuilder();
                            for (BigInteger zoneID : zoneIDs) {
                                zoneBuilder.or(QPayment.payment.account.zone.zoneId.eq(zoneID.longValue()));
                            }
                        }
                    }
                }


                //Credit adjustments for the month
                query = new JPAQuery(entityManager);
                BooleanBuilder creditAdjustmentBuilder = new BooleanBuilder();
                BooleanBuilder dateBuilder = new BooleanBuilder();
                dateBuilder.and(QPayment.payment.transactionDate.goe(from));
                dateBuilder.and(QPayment.payment.transactionDate.loe(to));
                creditAdjustmentBuilder.and(dateBuilder);
                creditAdjustmentBuilder.and(QPayment.payment.paymentType.paymentTypeId.eq(credit.getPaymentTypeId()));
                creditAdjustmentBuilder.and(zonePaymentsBuilder);
                creditAdjustmentTotal = query.from(QPayment.payment).where(creditAdjustmentBuilder).singleResult(QPayment.payment.amount.sum());

                //Debit adjustments for the month
                query = new JPAQuery(entityManager);
                BooleanBuilder debitAdjustmentBuilder = new BooleanBuilder();
                debitAdjustmentBuilder.and(dateBuilder);
                debitAdjustmentBuilder.and(zonePaymentsBuilder);
                debitAdjustmentBuilder.and(QPayment.payment.paymentType.paymentTypeId.eq(debit.getPaymentTypeId()));
                debitAdjustmentTotal = query.from(QPayment.payment).where(debitAdjustmentBuilder).singleResult(QPayment.payment.amount.sum());

                debitAdjustmentTotal = Math.abs(debitAdjustmentTotal);
                creditAdjustmentTotal = Math.abs(creditAdjustmentTotal) * -1;

                bsr.setCreditAdjustments(creditAdjustmentTotal);
                bsr.setDebitAdjustments(debitAdjustmentTotal);

                //Get all payments for the month
                query = new JPAQuery(entityManager);
                BooleanBuilder receiptsBuilder = new BooleanBuilder();
                receiptsBuilder.and(dateBuilder);
                receiptsBuilder.and(QPayment.payment.paymentType.unique.eq(Boolean.TRUE));
                receiptsBuilder.and(zonePaymentsBuilder);
                Double totalReceipts = query.from(QPayment.payment).where(receiptsBuilder).singleResult(QPayment.payment.amount.sum());
                bsr.setTotalPayments(totalReceipts);


                //bsr.set
                //Get active and inactive based on the last date of billing month
                //Active connections by end of month
                DateTime lastDayOfTheMonth = billingMonth.getMonth();
                Integer yearMonth = Integer.parseInt(lastDayOfTheMonth.toString("yyyyMM"));
                query = new JPAQuery(entityManager);
                BooleanBuilder activeAccountsBuilder = new BooleanBuilder();
                activeAccountsBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.code.eq(yearMonth));
                activeAccountsBuilder.and(QAccountBalanceRecord.accountBalanceRecord.cutOff.eq("Active"));
                activeAccountsBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.isSystem.eq(Boolean.TRUE));
                Long activeAccounts = query.from(QAccountBalanceRecord.accountBalanceRecord).where(activeAccountsBuilder).singleResult(QAccountBalanceRecord.accountBalanceRecord.count());
                bsr.setActiveAccounts(activeAccounts.intValue());

                //Inactive connections
                query = new JPAQuery(entityManager);
                BooleanBuilder inActiveAccountsBuilder = new BooleanBuilder();
                inActiveAccountsBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.code.eq(yearMonth));
                inActiveAccountsBuilder.and(QAccountBalanceRecord.accountBalanceRecord.cutOff.eq("Inactive"));
                inActiveAccountsBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.isSystem.eq(Boolean.TRUE));
                Long inActiveAccounts = query.from(QAccountBalanceRecord.accountBalanceRecord).where(inActiveAccountsBuilder).singleResult(QAccountBalanceRecord.accountBalanceRecord.count());
                bsr.setInactiveAccounts(inActiveAccounts.intValue());

                //Active account balances
                query = new JPAQuery(entityManager);
                BooleanBuilder activeAccountBalancesBuilder = new BooleanBuilder();
                activeAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.code.eq(yearMonth));
                activeAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.cutOff.eq("Active"));
                activeAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.isSystem.eq(Boolean.TRUE));
                activeAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.balance.gt(0));
                Double activeAccountBalances = query.from(QAccountBalanceRecord.accountBalanceRecord).where(activeAccountBalancesBuilder).singleResult(QAccountBalanceRecord.accountBalanceRecord.balance.sum());
                bsr.setBalancesActiveAccounts(activeAccountBalances);

                //Inactive account balances for the month
                query = new JPAQuery(entityManager);
                BooleanBuilder inactiveAccountBalancesBuilder = new BooleanBuilder();
                inactiveAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.code.eq(yearMonth));
                inactiveAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.cutOff.eq("Inactive"));
                inactiveAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.isSystem.eq(Boolean.TRUE));
                inactiveAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.balance.gt(0));
                Double inactiveAccountBalances = query.from(QAccountBalanceRecord.accountBalanceRecord).where(inactiveAccountBalancesBuilder).singleResult(QAccountBalanceRecord.accountBalanceRecord.balance.sum());
                bsr.setBalancesInactiveAccounts(inactiveAccountBalances);

                //Active metered accounts
                query = new JPAQuery(entityManager);
                BooleanBuilder activeMeteredBuilder = new BooleanBuilder();
                activeMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.code.eq(yearMonth));
                activeMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.cutOff.eq("Active"));
                activeMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.isSystem.eq(Boolean.TRUE));
                activeMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.metered.eq(Boolean.TRUE));
                Long activeMetered = query.from(QAccountBalanceRecord.accountBalanceRecord).where(activeMeteredBuilder).singleResult(QAccountBalanceRecord.accountBalanceRecord.count());
                bsr.setActiveMeteredAccounts(activeMetered.intValue());

                //Active not metered accounts
                query = new JPAQuery(entityManager);
                BooleanBuilder activeUnMeteredBuilder = new BooleanBuilder();
                activeUnMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.code.eq(yearMonth));
                activeUnMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.cutOff.eq("Inactive"));
                activeUnMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.isSystem.eq(Boolean.TRUE));
                activeUnMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.metered.eq(Boolean.FALSE));
                Long activeUnMetered = query.from(QAccountBalanceRecord.accountBalanceRecord).where(activeUnMeteredBuilder).singleResult(QAccountBalanceRecord.accountBalanceRecord.count());
                bsr.setActiveUnMeteredAccounts(activeUnMetered.intValue());


                bsr.setMeteredBilledActual(meteredBilledOnActual.intValue());
                bsr.setMeteredBilledAverage(meteredBilledOnAverage.intValue());

                //send report
                ReportObject report = new ReportObject();
                report.setDate(Calendar.getInstance());
                report.setAmount(totalAmountBilled);
                report.setMeterRent(meterRentTotal);
                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                report.setTitle(this.optionService.getOption("REPORT:WARIS").getValue());
                report.setContent(bsr);

                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(report);
                response = new RestResponse(responseObject, HttpStatus.OK);

            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return response;
    }

    public RestResponse getConsumersWithoutPhoneNumbers(RestRequestObject<ReportsParam> requestObject) {
        try {
            log.info("Generating consumers not with phone numbers report");
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_consumers_without_phone_numbers");
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

                List<Consumer> consumers = consumerRepository.findAllByPhoneNumber("");
                if (!consumers.isEmpty()) {
                    log.info(consumers.size() + " consumers found.");

                    List<BalancesReport> records = new ArrayList<>();

                    for (Consumer consumer : consumers) {
                        Boolean include = true;
                        if (include) {
                            BalancesReport balancesReport = new BalancesReport();
                            String consumerName = "";
                            if (StringUtils.isNotEmpty(consumer.getFirstName())) {
                                consumerName += consumer.getFirstName();
                            }

                            if (StringUtils.isNotEmpty(consumer.getMiddleName())) {
                                consumerName += " " + consumer.getMiddleName();
                            }

                            if (StringUtils.isNotEmpty(consumer.getLastName())) {
                                consumerName += " " + consumer.getLastName();
                            }

                            balancesReport.setAccName(consumerName);
                            balancesReport.setId(consumer.getConsumerId() + "");
                            balancesReport.setActive(true);
                            balancesReport.setZone("");
                            records.add(balancesReport);
                        }
                    }
                    log.info("Packaged report data...");

                    ReportObject report = new ReportObject();
                    report.setDate(Calendar.getInstance());
                    report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                    report.setTitle(this.optionService.getOption("REPORT:CONSUMERS_WITHOUT_PHONE_NUMBERS").getValue());
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

    public RestResponse getDetailedBillingCheckList(RestRequestObject<AccountsReportRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_billed_amount_detailed");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                AccountsReportRequest accountsReportRequest = requestObject.getObject();
                BooleanBuilder builder = new BooleanBuilder();

                if (accountsReportRequest.getBillingMonthId() == null) {
                    responseObject.setMessage("Billing month can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }
                BillingMonth billingMonth = billingMonthRepository.findOne(accountsReportRequest.getBillingMonthId());
                if (billingMonth == null) {
                    responseObject.setMessage("Invalid billing month resource");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                DateTime lastDayOfTheMonth = new DateTime().withMillis(billingMonth.getMonth().getMillis()).dayOfMonth().withMaximumValue();
                DateTime endMonthDate = lastDayOfTheMonth;
                //endMonthDate.setTimeInMillis(lastDayOfTheMonth.getMillis());

                builder.and(QBill.bill.billingMonth.billingMonthId.eq(accountsReportRequest.getBillingMonthId()));

                if (accountsReportRequest.getIsCutOff() != null) {
                    Boolean isActive = Boolean.TRUE;
                    if (accountsReportRequest.getIsCutOff()) {
                        isActive = Boolean.FALSE;
                    }
                    builder.and(QBill.bill.account.active.eq(isActive));
                }

                BooleanBuilder zoneBuilder = new BooleanBuilder();
                if (accountsReportRequest.getZoneId() != null) {
                    zoneBuilder.and(QBill.bill.account.zone.zoneId.eq(accountsReportRequest.getZoneId()));
                } else if (accountsReportRequest.getSchemeId() != null) {
                    Scheme scheme = schemeRepository.findOne(accountsReportRequest.getSchemeId());
                    if (scheme != null) {
                        List<Zone> zones = zoneRepository.findAllByScheme(scheme);
                        if (!zones.isEmpty()) {
                            for (Zone zone : zones) {
                                zoneBuilder.or(QBill.bill.account.zone.zoneId.eq(zone.getZoneId()));
                            }
                        }
                    }
                }
                builder.and(zoneBuilder);
                JPAQuery query = new JPAQuery(entityManager);
                List<Tuple> tupleList = query.from(QBill.bill).where(builder).list(QBill.bill.previousReading, QBill.bill.currentReading, QBill.bill.averageConsumption, QBill.bill.unitsBilled, QBill.bill.consumptionType, QBill.bill.billId, QBill.bill.account.accountId, QBill.bill.account.accNo, QBill.bill.meterRent, QBill.bill.amount, QBill.bill.totalBilled);
                List<ChargesRecord> chargesRecords = new ArrayList<>();
                if (!tupleList.isEmpty()) {
                    for (Tuple tuple : tupleList) {
                        Long accountId = tuple.get(QBill.bill.account.accountId);
                        Double balanceBroughtForward = 0d;
                        Double totalBilled = tuple.get(QBill.bill.totalBilled);
                        ChargesRecord chargesRecord = new ChargesRecord();
                        chargesRecord.setAccNo(tuple.get(QBill.bill.account.accNo));
                        chargesRecord.setAmount(tuple.get(QBill.bill.amount));
                        chargesRecord.setMeterRent(tuple.get(QBill.bill.meterRent));
                        chargesRecord.setTotalBill(tuple.get(QBill.bill.totalBilled));
                        chargesRecord.setOtherCharges(tuple.get(QBill.bill.totalBilled) - (tuple.get(QBill.bill.amount) + tuple.get(QBill.bill.meterRent)));
                        chargesRecord.setPreviousReading(tuple.get(QBill.bill.previousReading));
                        chargesRecord.setCurrentReading(tuple.get(QBill.bill.currentReading));
                        chargesRecord.setConsumption(tuple.get(QBill.bill.consumptionType));
                        chargesRecord.setUnits(tuple.get(QBill.bill.unitsBilled));
                        chargesRecord.setAverage(tuple.get(QBill.bill.averageConsumption));

                        String accountStatus = "Active";
                        String zone = "Not Available";
                        String consumerName = "";
                        String meterNo = "";
                        String meterSize = "";
                        String meterOwner = "";
                        String category = "";
                        query = new JPAQuery(entityManager);
                        BooleanBuilder meterBuilder = new BooleanBuilder();
                        meterBuilder.and(QAccount.account.accountId.eq(tuple.get(QBill.bill.account.accountId)));
                        List<Tuple> meterTupleList = query.from(QAccount.account).where(meterBuilder).list(QAccount.account.meter.meterNo, QAccount.account.meter.meterSize.size, QAccount.account.meter.meterOwner.name);
                        if (meterTupleList != null) {
                            if (!meterTupleList.isEmpty()) {
                                Tuple meterTuple = meterTupleList.get(0);
                                meterNo = meterTuple.get(QAccount.account.meter.meterNo);
                                meterSize = meterTuple.get(QAccount.account.meter.meterSize.size);
                                meterOwner = meterTuple.get(QAccount.account.meter.meterOwner.name);
                            }
                        }

                        query = new JPAQuery(entityManager);
                        BooleanBuilder categoryBuilder = new BooleanBuilder();
                        categoryBuilder.and(QAccount.account.accountId.eq(tuple.get(QBill.bill.account.accountId)));
                        String categoryName = query.from(QAccount.account).where(categoryBuilder).singleResult(QAccount.account.accountCategory.name);
                        if (StringUtils.isNotEmpty(categoryName)) {
                            category = categoryName;
                        }

                        query = new JPAQuery(entityManager);
                        BooleanBuilder accountBuilder = new BooleanBuilder();
                        accountBuilder.and(QAccount.account.accountId.eq(tuple.get(QBill.bill.account.accountId)));
                        List<Tuple> accountTupleList = query.from(QAccount.account).where(accountBuilder).list(QAccount.account.balanceBroughtForward, QAccount.account.active, QAccount.account.zone.name, QAccount.account.consumer.firstName, QAccount.account.consumer.middleName, QAccount.account.consumer.lastName);
                        if (!accountTupleList.isEmpty()) {
                            Tuple accountTuple = accountTupleList.get(0);
                            Boolean active = accountTuple.get(QAccount.account.active);
                            if (!active) {
                                accountStatus = "Inactive";
                            }

                            //Zone
                            zone = accountTuple.get(QAccount.account.zone.name);

                            //balance brought forward
                            balanceBroughtForward = accountTuple.get(QAccount.account.balanceBroughtForward);

                            //consumer name
                            consumerName = accountTuple.get(QAccount.account.consumer.firstName) + " " + accountTuple.get(QAccount.account.consumer.middleName) + " " + accountTuple.get(QAccount.account.consumer.lastName);
                            consumerName = consumerName.replace("null", "").toUpperCase();
                        }
                        //
                        chargesRecord.setAccountStatus(accountStatus);
                        chargesRecord.setZone(zone);
                        chargesRecord.setAccName(consumerName);
                        chargesRecord.setMeterNo(meterNo);
                        chargesRecord.setMeterSize(meterSize);
                        chargesRecord.setMeterOwner(meterOwner);
                        chargesRecord.setCategory(category);

                        Double reconnectionFee = 0d;
                        Double atOwnersRequestFee = 0d;
                        Double changeOfAccountName = 0d;
                        Double byPassFee = 0d;
                        Double bouncedChequeFee = 0d;
                        Double surchargeIrrigationFee = 0d;
                        Double surchageMisuseFee = 0d;
                        Double meterServicingFee = 0d;
                        Double totalFines = 0.0;

                        query = new JPAQuery(entityManager);
                        BooleanBuilder billItemsBuilder = new BooleanBuilder();
                        billItemsBuilder.and(QBillItem.billItem.bill.billId.eq(tuple.get(QBill.bill.billId)));
                        Iterable<BillItem> billItems = billItemRepository.findAll(billItemsBuilder);
                        for (BillItem billItem : billItems) {

                            totalFines += billItem.getAmount();

                            if (StringUtils.equalsIgnoreCase(billItem.getBillItemType().getName(), "Reconnection Fee")) {
                                chargesRecord.setReconnectionFee(billItem.getAmount());
                            } else if (StringUtils.equalsIgnoreCase(billItem.getBillItemType().getName(), "At Owners Request Fee")) {
                                chargesRecord.setAtOwnersRequestFee(billItem.getAmount());
                            } else if (StringUtils.equalsIgnoreCase(billItem.getBillItemType().getName(), "Change Of Account Name")) {
                                chargesRecord.setChangeOfAccountName(billItem.getAmount());
                            } else if (StringUtils.equalsIgnoreCase(billItem.getBillItemType().getName(), "By Pass Fee")) {
                                chargesRecord.setByPassFee(billItem.getAmount());
                            } else if (StringUtils.equalsIgnoreCase(billItem.getBillItemType().getName(), "Bounced Cheque Fee")) {
                                chargesRecord.setBouncedChequeFee(billItem.getAmount());
                            } else if (StringUtils.equalsIgnoreCase(billItem.getBillItemType().getName(), "Surcharge Irrigation")) {
                                chargesRecord.setSurchargeIrrigationFee(billItem.getAmount());
                            } else if (StringUtils.equalsIgnoreCase(billItem.getBillItemType().getName(), "Surcharge Missuse")) {
                                chargesRecord.setSurchageMisuseFee(billItem.getAmount());
                            } else if (StringUtils.equalsIgnoreCase(billItem.getBillItemType().getName(), "Meter Servicing")) {
                                chargesRecord.setMeterServicingFee(billItem.getAmount());
                            }
                        }


                        //Balance brought forward


                        //region payments by date
                        query = new JPAQuery(entityManager);
                        BooleanBuilder paymentsBuilder = new BooleanBuilder();
                        paymentsBuilder.and(QPayment.payment.account.accountId.eq(accountId));
                        paymentsBuilder.and(QPayment.payment.transactionDate.loe(endMonthDate));
                        Double paymentsLastDayOfTheMonth = query.from(QPayment.payment).where(paymentsBuilder).singleResult(QPayment.payment.amount.sum());
                        if (paymentsLastDayOfTheMonth == null) {
                            paymentsLastDayOfTheMonth = 0d;
                        }
                        //endregion

                        //region Get total bills by last date of the month
                        query = new JPAQuery(entityManager);
                        BooleanBuilder billsBuilder = new BooleanBuilder();
                        billsBuilder.and(QBill.bill.account.accountId.eq(accountId));
                        billsBuilder.and(QBill.bill.billingMonth.month.loe(endMonthDate));
                        Double billsLastDayOfTheMonth = query.from(QBill.bill).where(billsBuilder).singleResult(QBill.bill.totalBilled.sum());
                        if (billsLastDayOfTheMonth == null) {
                            billsLastDayOfTheMonth = 0.0;
                        }

                        //endregion

                        //region Get total meter rent by last date of the month

                        query = new JPAQuery(entityManager);
                        BooleanBuilder meterRentBuilder = new BooleanBuilder();
                        meterRentBuilder.and(QBill.bill.account.accountId.eq(accountId));
                        meterRentBuilder.and(QBill.bill.billingMonth.month.loe(endMonthDate));
                        Double meterRentOnLastDayOfTheMonth = query.from(QBill.bill).where(meterRentBuilder).singleResult(QBill.bill.meterRent.sum());
                        if (meterRentOnLastDayOfTheMonth == null) {
                            meterRentOnLastDayOfTheMonth = 0.0;
                        }

                        //endregion

                        //region Get total water sale by last date of the month

                        query = new JPAQuery(entityManager);
                        BooleanBuilder waterSaleBuilder = new BooleanBuilder();
                        waterSaleBuilder.and(QBill.bill.account.accountId.eq(accountId));
                        waterSaleBuilder.and(QBill.bill.billingMonth.month.loe(endMonthDate));
                        Double waterSaleOnLastDayOfTheMonth = query.from(QBill.bill).where(waterSaleBuilder).singleResult(QBill.bill.amount.sum());
                        if (waterSaleOnLastDayOfTheMonth == null) {
                            waterSaleOnLastDayOfTheMonth = 0.0;
                        }

                        //endregion

                        //region Get total fines by last date of the month

                        query = new JPAQuery(entityManager);
                        BooleanBuilder finesBuilder = new BooleanBuilder();
                        finesBuilder.and(QBillItem.billItem.bill.account.accountId.eq(accountId));
                        finesBuilder.and(QBillItem.billItem.bill.billingMonth.month.loe(endMonthDate));
                        Double finesOnLastDayOfTheMonth = query.from(QBillItem.billItem).where(finesBuilder).singleResult(QBillItem.billItem.amount.sum());
                        if (finesOnLastDayOfTheMonth == null) {
                            finesOnLastDayOfTheMonth = 0.0;
                        }

                        //endregion


                        Double balanceLastDayOfTheMonth = (balanceBroughtForward + billsLastDayOfTheMonth) - paymentsLastDayOfTheMonth;
                        Double balanceBeforeBill = balanceLastDayOfTheMonth - totalBilled;
                        chargesRecord.setBalanceBroughtForward(balanceBeforeBill);

                        //accountService.getAccountBalanceByDate()
                        if (accountId == 1719) {
                            String s = "";
                        }
                        Double amountToAllocate = new Double(paymentsLastDayOfTheMonth);

                        if (amountToAllocate > 0) {
                            amountToAllocate -= balanceBroughtForward;
                        }

                        //fines
                        finesOnLastDayOfTheMonth -= totalFines;
                        if (amountToAllocate >= finesOnLastDayOfTheMonth) {
                            chargesRecord.setFineArrears(0.0);
                            amountToAllocate -= finesOnLastDayOfTheMonth;
                        } else {
                            finesOnLastDayOfTheMonth -= amountToAllocate;
                            chargesRecord.setFineArrears(finesOnLastDayOfTheMonth);
                            amountToAllocate = 0.0;
                        }


                        //Meter Rent
                        meterRentOnLastDayOfTheMonth -= chargesRecord.getMeterRent();
                        if (amountToAllocate >= meterRentOnLastDayOfTheMonth) {
                            chargesRecord.setMeterRentArrears(0.0);
                            amountToAllocate -= meterRentOnLastDayOfTheMonth;
                        } else {
                            meterRentOnLastDayOfTheMonth -= amountToAllocate;
                            chargesRecord.setMeterRentArrears(meterRentOnLastDayOfTheMonth);
                            amountToAllocate = 0.0;
                        }


                        //Water sale
                        waterSaleOnLastDayOfTheMonth -= chargesRecord.getAmount();
                        if (amountToAllocate >= waterSaleOnLastDayOfTheMonth) {
                            chargesRecord.setWaterSaleArrears(0.0);
                            amountToAllocate -= waterSaleOnLastDayOfTheMonth;
                        } else {
                            waterSaleOnLastDayOfTheMonth -= amountToAllocate;
                            chargesRecord.setWaterSaleArrears(waterSaleOnLastDayOfTheMonth);
                            amountToAllocate = 0.0;
                        }

                        chargesRecords.add(chargesRecord);
                    }
                }

                log.info("Packaged report data...");
                ReportObject report = new ReportObject();
                report.setDate(Calendar.getInstance());

                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                report.setTitle(this.optionService.getOption("REPORT:BILLED_AMOUNT_DETAILED").getValue());
                report.setContent(chargesRecords);
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

    public RestResponse getAccountsToActivate(RestRequestObject<AccountsReportRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_accounts_to_activate");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                AccountsReportRequest accountsReportRequest = requestObject.getObject();

                if (accountsReportRequest.getFromDate() == null) {
                    responseObject.setMessage("From date can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (accountsReportRequest.getToDate() == null) {
                    responseObject.setMessage("To date can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (accountsReportRequest.getFromDate().isAfter(accountsReportRequest.getToDate())) {
                    responseObject.setMessage("From date can not be after to date");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                BooleanBuilder builder = new BooleanBuilder();

                if (accountsReportRequest.getSchemeId() != null) {
                    builder.and(QToActivate.toActivate.account.zone.scheme.schemeId.eq(accountsReportRequest.getSchemeId()));
                }

                if (accountsReportRequest.getZoneId() != null) {
                    builder.and(QToActivate.toActivate.account.zone.zoneId.eq(accountsReportRequest.getZoneId()));
                }

                builder.and(QToActivate.toActivate.transactionDate.goe(accountsReportRequest.getFromDate()));
                builder.and(QToActivate.toActivate.transactionDate.loe(accountsReportRequest.getToDate()));

                JPAQuery query = new JPAQuery(entityManager);

                List<Tuple> tupleList = query.from(QToActivate.toActivate).where(builder).list(
                        QToActivate.toActivate.account.accNo,
                        QToActivate.toActivate.account.consumer.firstName,
                        QToActivate.toActivate.account.consumer.middleName,
                        QToActivate.toActivate.account.consumer.lastName,
                        QToActivate.toActivate.account.zone.name,
                        QToActivate.toActivate.transactionDate);

                List<ToActivateRecord> toActivateRecords = new ArrayList<>();
                if (!tupleList.isEmpty()) {
                    for (Tuple tuple : tupleList) {
                        ToActivateRecord toActivateRecord = new ToActivateRecord();
                        String consumerName = "";
                        consumerName = tuple.get(QToActivate.toActivate.account.consumer.firstName) + " " + tuple.get(QToActivate.toActivate.account.consumer.middleName) + " " + tuple.get(QToActivate.toActivate.account.consumer.lastName);
                        consumerName = consumerName.replace("null", "").toUpperCase();
                        toActivateRecord.setAccName(consumerName);
                        toActivateRecord.setZone(tuple.get(QToActivate.toActivate.account.zone.name));
                        toActivateRecord.setPaidOn(tuple.get(QToActivate.toActivate.transactionDate));

                        toActivateRecords.add(toActivateRecord);
                    }
                }

                log.info("Packaged report data...");
                ReportObject report = new ReportObject();
                report.setDate(Calendar.getInstance());

                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                report.setTitle(this.optionService.getOption("REPORT:ACCOUNTS_TO_ACTIVATE").getValue());
                report.setContent(toActivateRecords);
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

//    public RestResponse getWarisByAccountCategories(RestRequestObject<AccountsReportRequest> requestObject) {
//        try {
//            response = authManager.tokenValid(requestObject.getToken());
//            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
//                response = authManager.grant(requestObject.getToken(), "report_waris");
//                if (response.getStatusCode() != HttpStatus.OK) {
//                    return response;
//                }
//
//                log.info("Getting WARIS by account categories report...");
//
//                AccountsReportRequest accountsReportRequest = requestObject.getObject();
//
//
//                if (accountsReportRequest.getFromDate() == null) {
//                    responseObject.setMessage("Start date can not be empty");
//                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
//                    return response;
//                }
//
//                if (accountsReportRequest.getToDate() == null) {
//                    responseObject.setMessage("End date can not be empty");
//                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
//                    return response;
//                }
//                JPAQuery query = new JPAQuery(entityManager);
//                List<Long> accountCategoryIds = query.from(QAccountCategory.accountCategory).orderBy(QAccountCategory.accountCategory.name.asc()).list(QAccountCategory.accountCategory.categoryId);
//
//                List<AccountCategory> accountCategories = new ArrayList<>();
//
//                BillingSummaryRecord bsr = new BillingSummaryRecord();
//
//
//                BooleanBuilder billsBuilder = new BooleanBuilder();
//                billsBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
//
//                BooleanBuilder zoneBillsBuilder = new BooleanBuilder();
//
//                if (accountsReportRequest.getZoneId() != null) {
//                    zoneBillsBuilder.and(QBill.bill.account.zone.zoneId.eq(accountsReportRequest.getZoneId()));
//                } else if (accountsReportRequest.getSchemeId() != null) {
//                    Scheme scheme = schemeRepository.findOne(accountsReportRequest.getSchemeId());
//                    if (scheme != null) {
//                        List<BigInteger> zoneIDs = zoneRepository.findAllBySchemeId(scheme.getSchemeId());
//                        if (!zoneIDs.isEmpty()) {
//                            for (BigInteger zoneID : zoneIDs) {
//                                zoneBillsBuilder.or(QBill.bill.account.zone.zoneId.eq(zoneID.longValue()));
//                            }
//                        }
//                    }
//                }
//
//                billsBuilder.and(zoneBillsBuilder);
//
//                List<Long> bills = query.from(QBill.bill).where(billsBuilder).list(QBill.bill.billId);
//                log.info("Bills " + bills.size() + " found.");
//                if (bills == null || bills.isEmpty()) {
//                    responseObject.setMessage("No content found");
//                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
//                    return response;
//                }
//
//                Double totalAmountBilled = 0.0;
//
//
//                //Get total amount billed on actual
//                query = new JPAQuery(entityManager);
//                BooleanBuilder billedOnActualBuilder = new BooleanBuilder();
//                billedOnActualBuilder.and(QBill.bill.consumptionType.equalsIgnoreCase("Actual"));
//                billedOnActualBuilder.and(zoneBillsBuilder);
//                billedOnActualBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
//                Double billedOnActualTotal = query.from(QBill.bill).where(billedOnActualBuilder).singleResult(QBill.bill.amount.sum());
//                bsr.setBilledOnActual(billedOnActualTotal);
//                totalAmountBilled += billedOnActualTotal;
//
//                //Get total Billed on average
//                query = new JPAQuery(entityManager);
//                BooleanBuilder billedOnAverageBuilder = new BooleanBuilder();
//                billedOnAverageBuilder.and(QBill.bill.consumptionType.equalsIgnoreCase("Average"));
//                billedOnAverageBuilder.and(zoneBillsBuilder);
//                billedOnAverageBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
//                Double billedOnAverageTotal = query.from(QBill.bill).where(billedOnAverageBuilder).singleResult(QBill.bill.amount.sum());
//                bsr.setBilledOnEstimate(billedOnAverageTotal);
//                totalAmountBilled += billedOnAverageTotal;
//
//                //Get metered billed on actual
//                query = new JPAQuery(entityManager);
//                BooleanBuilder meteredOnActualBuilder = new BooleanBuilder();
//                meteredOnActualBuilder.and(QBill.bill.consumptionType.equalsIgnoreCase("Actual"));
//                meteredOnActualBuilder.and(zoneBillsBuilder);
//                meteredOnActualBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
//                meteredOnActualBuilder.and(QBill.bill.account.meter.isNotNull());
//                Long meteredBilledOnActual = query.from(QBill.bill).where(meteredOnActualBuilder).singleResult(QBill.bill.count());
//
//                //Get metered billed on actual
//                query = new JPAQuery(entityManager);
//                BooleanBuilder meteredOnAverageBuilder = new BooleanBuilder();
//                meteredOnAverageBuilder.and(QBill.bill.consumptionType.equalsIgnoreCase("Average"));
//                meteredOnAverageBuilder.and(zoneBillsBuilder);
//                meteredOnAverageBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
//                meteredOnAverageBuilder.and(QBill.bill.account.meter.isNotNull());
//                Long meteredBilledOnAverage = query.from(QBill.bill).where(meteredOnAverageBuilder).singleResult(QBill.bill.count());
//
//
//                //Get units billed on actual
//                query = new JPAQuery(entityManager);
//                BooleanBuilder unitsOnActualBuilder = new BooleanBuilder();
//                unitsOnActualBuilder.and(QBill.bill.consumptionType.equalsIgnoreCase("Actual"));
//                unitsOnActualBuilder.and(zoneBillsBuilder);
//                unitsOnActualBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
//                Double unitsBilledOnActual = query.from(QBill.bill).where(unitsOnActualBuilder).singleResult(QBill.bill.unitsBilled.sum());
//                bsr.setUnitsActualConsumption(unitsBilledOnActual);
//
//                //Get units billed on average
//                query = new JPAQuery(entityManager);
//                BooleanBuilder unitsOnAverageBuilder = new BooleanBuilder();
//                unitsOnAverageBuilder.and(QBill.bill.consumptionType.equalsIgnoreCase("Average"));
//                unitsOnAverageBuilder.and(zoneBillsBuilder);
//                unitsOnAverageBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
//                Double unitsOnAverage = query.from(QBill.bill).where(unitsOnAverageBuilder).singleResult(QBill.bill.unitsBilled.sum());
//                bsr.setUnitsEstimatedConsumption(unitsOnAverage);
//
//
//                //Get total Meter Rent
//                query = new JPAQuery(entityManager);
//                BooleanBuilder meterRentBuilder = new BooleanBuilder();
//                meterRentBuilder.and(zoneBillsBuilder);
//                meterRentBuilder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
//                Double meterRentTotal = query.from(QBill.bill).where(meterRentBuilder).singleResult(QBill.bill.meterRent.sum());
//                totalAmountBilled += meterRentTotal;
//
//                //Get Other charges
//
//                //Reconnection fee
//                query = new JPAQuery(entityManager);
//                BooleanBuilder reconnectionFeeBuilder = new BooleanBuilder();
//                reconnectionFeeBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("Reconnection Fee").getBillTypeId()));
//                reconnectionFeeBuilder.and(QBillItem.billItem.bill.billId.in(bills));
//                Double reconnectionFeeTotal = query.from(QBillItem.billItem).where(reconnectionFeeBuilder).singleResult(QBillItem.billItem.amount.sum());
//                if (reconnectionFeeTotal == null) reconnectionFeeTotal = 0d;
//                bsr.setReconnectionFee(reconnectionFeeTotal);
//                totalAmountBilled += reconnectionFeeTotal;
//
//                //At Owners Request Fee
//                query = new JPAQuery(entityManager);
//                BooleanBuilder ownersRequestFeeBuilder = new BooleanBuilder();
//                ownersRequestFeeBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("At Owners Request Fee").getBillTypeId()));
//                ownersRequestFeeBuilder.and(QBillItem.billItem.bill.billId.in(bills));
//                Double ownersRequestFeeTotal = query.from(QBillItem.billItem).where(ownersRequestFeeBuilder).singleResult(QBillItem.billItem.amount.sum());
//                if (ownersRequestFeeTotal == null) ownersRequestFeeTotal = 0d;
//                bsr.setAtOwnersRequestFee(ownersRequestFeeTotal);
//                totalAmountBilled += ownersRequestFeeTotal;
//
//                //Change Of Account Name
//                query = new JPAQuery(entityManager);
//                BooleanBuilder changeOfAccountBuilder = new BooleanBuilder();
//                changeOfAccountBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("Change Of Account Name").getBillTypeId()));
//                changeOfAccountBuilder.and(QBillItem.billItem.bill.billId.in(bills));
//                Double changeOfAccountFeeTotal = query.from(QBillItem.billItem).where(changeOfAccountBuilder).singleResult(QBillItem.billItem.amount.sum());
//                if (changeOfAccountFeeTotal == null) changeOfAccountFeeTotal = 0d;
//                bsr.setChangeOfAccountName(changeOfAccountFeeTotal);
//                totalAmountBilled += changeOfAccountFeeTotal;
//
//                //By Pass Fee
//                query = new JPAQuery(entityManager);
//                BooleanBuilder byPassFeeBuilder = new BooleanBuilder();
//                byPassFeeBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("By Pass Fee").getBillTypeId()));
//                byPassFeeBuilder.and(QBillItem.billItem.bill.billId.in(bills));
//                Double byPassFeeTotal = query.from(QBillItem.billItem).where(byPassFeeBuilder).singleResult(QBillItem.billItem.amount.sum());
//                if (byPassFeeTotal == null) byPassFeeTotal = 0d;
//                bsr.setByPassFee(byPassFeeTotal);
//                totalAmountBilled += byPassFeeTotal;
//
//                //Bounced Cheque Fee
//                query = new JPAQuery(entityManager);
//                BooleanBuilder bouncedChequeFeeBuilder = new BooleanBuilder();
//                bouncedChequeFeeBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("Bounced Cheque Fee").getBillTypeId()));
//                bouncedChequeFeeBuilder.and(QBillItem.billItem.bill.billId.in(bills));
//                Double bouncedChequeFeeTotal = query.from(QBillItem.billItem).where(bouncedChequeFeeBuilder).singleResult(QBillItem.billItem.amount.sum());
//                if (bouncedChequeFeeTotal == null) bouncedChequeFeeTotal = 0d;
//                bsr.setBouncedChequeFee(bouncedChequeFeeTotal);
//                totalAmountBilled += bouncedChequeFeeTotal;
//
//                //Surcharge Irrigation
//                query = new JPAQuery(entityManager);
//                BooleanBuilder surchageIrrigationFeeBuilder = new BooleanBuilder();
//                surchageIrrigationFeeBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("Surcharge Irrigation").getBillTypeId()));
//                surchageIrrigationFeeBuilder.and(QBillItem.billItem.bill.billId.in(bills));
//                Double surchageIrrigationFeeTotal = query.from(QBillItem.billItem).where(surchageIrrigationFeeBuilder).singleResult(QBillItem.billItem.amount.sum());
//                if (surchageIrrigationFeeTotal == null) surchageIrrigationFeeTotal = 0d;
//                bsr.setSurchargeIrrigation(surchageIrrigationFeeTotal);
//                totalAmountBilled += surchageIrrigationFeeTotal;
//
//                //Surcharge Missuse
//                query = new JPAQuery(entityManager);
//                BooleanBuilder surchageMissuseFeeBuilder = new BooleanBuilder();
//                surchageMissuseFeeBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("Surcharge Missuse").getBillTypeId()));
//                surchageMissuseFeeBuilder.and(QBillItem.billItem.bill.billId.in(bills));
//                Double surchageMissuseFeeTotal = query.from(QBillItem.billItem).where(surchageMissuseFeeBuilder).singleResult(QBillItem.billItem.amount.sum());
//                if (surchageMissuseFeeTotal == null) surchageMissuseFeeTotal = 0d;
//                bsr.setSurchargeMissuse(surchageMissuseFeeTotal);
//                totalAmountBilled += surchageMissuseFeeTotal;
//
//                //Meter Servicing
//                query = new JPAQuery(entityManager);
//                BooleanBuilder meterServicingFeeBuilder = new BooleanBuilder();
//                meterServicingFeeBuilder.and(QBillItem.billItem.billItemType.billTypeId.eq(billItemTypeRepository.findByName("Meter Servicing").getBillTypeId()));
//                meterServicingFeeBuilder.and(QBillItem.billItem.bill.billId.in(bills));
//                Double meterServicingFeeTotal = query.from(QBillItem.billItem).where(meterServicingFeeBuilder).singleResult(QBillItem.billItem.amount.sum());
//                if (meterServicingFeeTotal == null) meterServicingFeeTotal = 0d;
//                bsr.setMeterServicing(meterServicingFeeTotal);
//                totalAmountBilled += meterServicingFeeTotal;
//
//
////                for (Long billId : bills) {
////                    Bill b = billRepository.findOne(billId);
////                    //accountRepository.fi
////                    Long meterId = 0l;
////                    Boolean isMetered = false;
////
////                    try {
////                        if (b.getAccount().isMetered()) {
////                            isMetered = true;
////                        }
////                    } catch (Exception ex) {
////                        log.error(ex.getMessage());
////                    }
////
////                    totalAmountBilled += b.getAmount();
////                    totalMeterRent += b.getMeterRent();
////                    //amount billed
////                    if (b.getConsumptionType().compareToIgnoreCase("Actual") == 0) {
////                        //amount billed on actual
////                        bsr.setBilledOnActual(bsr.getBilledOnActual() + b.getAmount());
////
////                        //units billed on actual
////                        bsr.setUnitsActualConsumption(bsr.getUnitsActualConsumption() + b.getUnitsBilled());
////                        if (isMetered) {
////                            bsr.setMeteredBilledActual(bsr.getMeteredBilledActual() + 1);
////                        }
////
////                    } else if (b.getConsumptionType().compareToIgnoreCase("Average") == 0) {
////                        //amount billed on average
////                        bsr.setBilledOnEstimate(bsr.getBilledOnEstimate() + b.getAmount());
////
////                        //units billed on average
////                        bsr.setUnitsEstimatedConsumption(bsr.getUnitsEstimatedConsumption() + b.getAverageConsumption());
////                        if (isMetered) {
////                            bsr.setMeteredBilledAverage(bsr.getMeteredBilledAverage() + 1);
////                        }
////                    }
////
////                    //Other charges
////                    if (!b.getBillItems().isEmpty()) {
////                        for (BillItem bi : b.getBillItems()) {
////                            totalAmountBilled += bi.getAmount();
////                            if (bi.getBillItemType().getName().compareToIgnoreCase("Reconnection Fee") == 0) {
////                                bsr.setReconnectionFee(bsr.getReconnectionFee() + bi.getAmount());
////                            } else if (bi.getBillItemType().getName().compareToIgnoreCase("At Owners Request Fee") == 0) {
////                                bsr.setAtOwnersRequestFee(bsr.getAtOwnersRequestFee() + bi.getAmount());
////                            } else if (bi.getBillItemType().getName().compareToIgnoreCase("Change Of Account Name") == 0) {
////                                bsr.setChangeOfAccountName(bsr.getChangeOfAccountName() + bi.getAmount());
////                            } else if (bi.getBillItemType().getName().compareToIgnoreCase("By Pass Fee") == 0) {
////                                bsr.setByPassFee(bsr.getByPassFee() + bi.getAmount());
////                            } else if (bi.getBillItemType().getName().compareToIgnoreCase("Bounced Cheque Fee") == 0) {
////                                bsr.setBouncedChequeFee(bsr.getBouncedChequeFee() + bi.getAmount());
////                            } else if (bi.getBillItemType().getName().compareToIgnoreCase("Surcharge Irrigation") == 0) {
////                                bsr.setSurchargeIrrigation(bsr.getSurchargeIrrigation() + bi.getAmount());
////                            } else if (bi.getBillItemType().getName().compareToIgnoreCase("Surcharge Missuse") == 0) {
////                                bsr.setSurchargeMissuse(bsr.getSurchargeMissuse() + bi.getAmount());
////                            } else if (bi.getBillItemType().getName().compareToIgnoreCase("Meter Servicing") == 0) {
////                                bsr.setMeterServicing(bsr.getMeterServicing() + bi.getAmount());
////                            }
////                        }
////                    }
////
////                }
//
//                PaymentType credit = paymentTypeRepository.findByName("CREDIT");
//                PaymentType debit = paymentTypeRepository.findByName("DEBIT");
//                Double creditAdjustmentTotal = 0d;
//                Double debitAdjustmentTotal = 0d;
//                DateTime fromDate = new DateTime().withMillis(billingMonth.getMonth().getMillis()).withTimeAtStartOfDay().dayOfMonth().withMinimumValue();
//                DateTime toDate = fromDate.dayOfMonth().withMaximumValue();
//
//                DateTime from = fromDate;
//                DateTime to = toDate;
//
//                BooleanBuilder zonePaymentsBuilder = new BooleanBuilder();
//                if (accountsReportRequest.getZoneId() != null) {
//                    zonePaymentsBuilder.and(QPayment.payment.account.zone.zoneId.eq(accountsReportRequest.getZoneId()));
//                } else if (accountsReportRequest.getSchemeId() != null) {
//                    Scheme scheme = schemeRepository.findOne(accountsReportRequest.getSchemeId());
//                    if (scheme != null) {
//                        List<BigInteger> zoneIDs = zoneRepository.findAllBySchemeId(scheme.getSchemeId());
//                        if (!zoneIDs.isEmpty()) {
//                            BooleanBuilder zoneBuilder = new BooleanBuilder();
//                            for (BigInteger zoneID : zoneIDs) {
//                                zoneBuilder.or(QPayment.payment.account.zone.zoneId.eq(zoneID.longValue()));
//                            }
//                        }
//                    }
//                }
//
//
//                //Credit adjustments for the month
//                query = new JPAQuery(entityManager);
//                BooleanBuilder creditAdjustmentBuilder = new BooleanBuilder();
//                BooleanBuilder dateBuilder = new BooleanBuilder();
//                dateBuilder.and(QPayment.payment.transactionDate.goe(from));
//                dateBuilder.and(QPayment.payment.transactionDate.loe(to));
//                creditAdjustmentBuilder.and(dateBuilder);
//                creditAdjustmentBuilder.and(QPayment.payment.paymentType.paymentTypeId.eq(credit.getPaymentTypeId()));
//                creditAdjustmentBuilder.and(zonePaymentsBuilder);
//                creditAdjustmentTotal = query.from(QPayment.payment).where(creditAdjustmentBuilder).singleResult(QPayment.payment.amount.sum());
//
//                //Debit adjustments for the month
//                query = new JPAQuery(entityManager);
//                BooleanBuilder debitAdjustmentBuilder = new BooleanBuilder();
//                debitAdjustmentBuilder.and(dateBuilder);
//                debitAdjustmentBuilder.and(zonePaymentsBuilder);
//                debitAdjustmentBuilder.and(QPayment.payment.paymentType.paymentTypeId.eq(debit.getPaymentTypeId()));
//                debitAdjustmentTotal = query.from(QPayment.payment).where(debitAdjustmentBuilder).singleResult(QPayment.payment.amount.sum());
//
//                debitAdjustmentTotal = Math.abs(debitAdjustmentTotal);
//                creditAdjustmentTotal = Math.abs(creditAdjustmentTotal) * -1;
//
//                bsr.setCreditAdjustments(creditAdjustmentTotal);
//                bsr.setDebitAdjustments(debitAdjustmentTotal);
//
//                //Get all payments for the month
//                query = new JPAQuery(entityManager);
//                BooleanBuilder receiptsBuilder = new BooleanBuilder();
//                receiptsBuilder.and(dateBuilder);
//                receiptsBuilder.and(QPayment.payment.paymentType.unique.eq(Boolean.TRUE));
//                receiptsBuilder.and(zonePaymentsBuilder);
//                Double totalReceipts = query.from(QPayment.payment).where(receiptsBuilder).singleResult(QPayment.payment.amount.sum());
//                bsr.setTotalPayments(totalReceipts);
//
//
//                //bsr.set
//                //Get active and inactive based on the last date of billing month
//                //Active connections by end of month
//                DateTime lastDayOfTheMonth = billingMonth.getMonth();
//                Integer yearMonth = Integer.parseInt(lastDayOfTheMonth.toString("yyyyMM"));
//                query = new JPAQuery(entityManager);
//                BooleanBuilder activeAccountsBuilder = new BooleanBuilder();
//                activeAccountsBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.code.eq(yearMonth));
//                activeAccountsBuilder.and(QAccountBalanceRecord.accountBalanceRecord.cutOff.eq("Active"));
//                activeAccountsBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.isSystem.eq(Boolean.TRUE));
//                Long activeAccounts = query.from(QAccountBalanceRecord.accountBalanceRecord).where(activeAccountsBuilder).singleResult(QAccountBalanceRecord.accountBalanceRecord.count());
//                bsr.setActiveAccounts(activeAccounts.intValue());
//
//                //Inactive connections
//                query = new JPAQuery(entityManager);
//                BooleanBuilder inActiveAccountsBuilder = new BooleanBuilder();
//                inActiveAccountsBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.code.eq(yearMonth));
//                inActiveAccountsBuilder.and(QAccountBalanceRecord.accountBalanceRecord.cutOff.eq("Inactive"));
//                inActiveAccountsBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.isSystem.eq(Boolean.TRUE));
//                Long inActiveAccounts = query.from(QAccountBalanceRecord.accountBalanceRecord).where(inActiveAccountsBuilder).singleResult(QAccountBalanceRecord.accountBalanceRecord.count());
//                bsr.setInactiveAccounts(inActiveAccounts.intValue());
//
//                //Active account balances
//                query = new JPAQuery(entityManager);
//                BooleanBuilder activeAccountBalancesBuilder = new BooleanBuilder();
//                activeAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.code.eq(yearMonth));
//                activeAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.cutOff.eq("Active"));
//                activeAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.isSystem.eq(Boolean.TRUE));
//                activeAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.balance.gt(0));
//                Double activeAccountBalances = query.from(QAccountBalanceRecord.accountBalanceRecord).where(activeAccountBalancesBuilder).singleResult(QAccountBalanceRecord.accountBalanceRecord.balance.sum());
//                bsr.setBalancesActiveAccounts(activeAccountBalances);
//
//                //Inactive account balances for the month
//                query = new JPAQuery(entityManager);
//                BooleanBuilder inactiveAccountBalancesBuilder = new BooleanBuilder();
//                inactiveAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.code.eq(yearMonth));
//                inactiveAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.cutOff.eq("Inactive"));
//                inactiveAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.isSystem.eq(Boolean.TRUE));
//                inactiveAccountBalancesBuilder.and(QAccountBalanceRecord.accountBalanceRecord.balance.gt(0));
//                Double inactiveAccountBalances = query.from(QAccountBalanceRecord.accountBalanceRecord).where(inactiveAccountBalancesBuilder).singleResult(QAccountBalanceRecord.accountBalanceRecord.balance.sum());
//                bsr.setBalancesInactiveAccounts(inactiveAccountBalances);
//
//                //Active metered accounts
//                query = new JPAQuery(entityManager);
//                BooleanBuilder activeMeteredBuilder = new BooleanBuilder();
//                activeMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.code.eq(yearMonth));
//                activeMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.cutOff.eq("Active"));
//                activeMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.isSystem.eq(Boolean.TRUE));
//                activeMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.metered.eq(Boolean.TRUE));
//                Long activeMetered = query.from(QAccountBalanceRecord.accountBalanceRecord).where(activeMeteredBuilder).singleResult(QAccountBalanceRecord.accountBalanceRecord.count());
//                bsr.setActiveMeteredAccounts(activeMetered.intValue());
//
//                //Active not metered accounts
//                query = new JPAQuery(entityManager);
//                BooleanBuilder activeUnMeteredBuilder = new BooleanBuilder();
//                activeUnMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.code.eq(yearMonth));
//                activeUnMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.cutOff.eq("Inactive"));
//                activeUnMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.isSystem.eq(Boolean.TRUE));
//                activeUnMeteredBuilder.and(QAccountBalanceRecord.accountBalanceRecord.metered.eq(Boolean.FALSE));
//                Long activeUnMetered = query.from(QAccountBalanceRecord.accountBalanceRecord).where(activeUnMeteredBuilder).singleResult(QAccountBalanceRecord.accountBalanceRecord.count());
//                bsr.setActiveUnMeteredAccounts(activeUnMetered.intValue());
//
//
//                bsr.setMeteredBilledActual(meteredBilledOnActual.intValue());
//                bsr.setMeteredBilledAverage(meteredBilledOnAverage.intValue());
//
//                //send report
//                ReportObject report = new ReportObject();
//                report.setDate(Calendar.getInstance());
//                report.setAmount(totalAmountBilled);
//                report.setMeterRent(meterRentTotal);
//                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
//                report.setTitle(this.optionService.getOption("REPORT:WARIS").getValue());
//                report.setContent(bsr);
//
//                responseObject.setMessage("Fetched data successfully");
//                responseObject.setPayload(report);
//                response = new RestResponse(responseObject, HttpStatus.OK);
//
//            }
//        } catch (Exception ex) {
//            responseObject.setMessage(ex.getLocalizedMessage());
//            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
//            log.error(ex.getLocalizedMessage());
//            ex.printStackTrace();
//        }
//        return response;
//    }
}