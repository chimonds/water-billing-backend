package ke.co.suncha.simba.aqua.services;

import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.security.Credential;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.models.BillItemType;
import ke.co.suncha.simba.aqua.models.BillingMonth;
import ke.co.suncha.simba.aqua.models.PaymentType;
import ke.co.suncha.simba.aqua.models.Zone;
import ke.co.suncha.simba.aqua.repository.*;
import ke.co.suncha.simba.aqua.stats.*;
import ke.co.suncha.simba.aqua.utils.MobileClientRequest;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> Created by manyala on 5/19/15.
 */
@Service
public class StatsService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BillItemTypeRepository billItemTypeRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillingMonthRepository billingMonthRepository;

    @Autowired
    private PaymentTypeRepository paymentTypeRepository;

    @Autowired
    private SimbaOptionService optionService;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private MPESARepository mpesaRepository;

    @Autowired
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    AccountService accountService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SMSService smsService;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();
    private TopView topView = new TopView();
    private BillsPaymentsLineGraph billsPaymentsLineGraph = new BillsPaymentsLineGraph();
    private ZonesBarGraph zonesBarGraph = new ZonesBarGraph();
    private ZoneBalances zoneBalances = new ZoneBalances();

    @Scheduled(fixedDelay = 300000)
    private void populateStats() {
        populateAccountsConsumersCount();
        populatePaidThisMonth();
        populatePaidToday();
        populatePaidLastMonth();
        //uploadMobileClientStats();
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 600000)
    private void populateHuge() {
        populateXaxis();
        populateXZonesBarGraph();
        populateZonesBarGraph();
        populateBillsPaymentsLineGraph();
        populateZonesAccountBalancesBarGraph();
    }

    private void populateAccountsConsumersCount() {
        try {
            topView.setAccounts(accountRepository.count());
            topView.setConsumers(consumerRepository.count());
            topView.setActive(accountRepository.countByActive(true));
            topView.setInactive(accountRepository.countByActive(false));
            topView.setBalances(accountRepository.getAllBalances());
            topView.setCreditBalances(accountRepository.getAllCreditBalances());
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private void uploadMobileClientStats() {
        try {
            //topView
            //log.info("Uploading topview stats");
            RestTemplate restTemplate = new RestTemplate();

            MobileClientRequest request = new MobileClientRequest();
            Credential credential = new Credential();

            credential.setUsername(optionService.getOption("MOBILE_CLIENT_USERNAME").getValue());
            credential.setPassword(optionService.getOption("MOBILE_CLIENT_KEY").getValue());
            request.setLogin(credential);
            request.setPayload(topView);

            String url = optionService.getOption("MOBILE_CLIENT_ENDPOINT").getValue() + "/upload_summary_stats";
            String jsonResponse = restTemplate.postForObject(url, request, String.class);
            //log.info("Response:" + jsonResponse);

            //2. Convert JSON to Java object
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private void populatePaidThisMonth() {
        try {
            DateTime fromDate = new DateTime().dayOfMonth().withMinimumValue();
            DateTime endDate = new DateTime().dayOfMonth().withMaximumValue();
            Double total = 0.0;
            //Double dbAmount = paymentRepository.getTotalAmountPaidByDate(fromDate.toString("yyyy-MM-dd"), endDate.toString("yyyy-MM-dd"));
            Double dbAmount = paymentRepository.getTotalReceiptsByDate(fromDate.toString("yyyy-MM-dd"), endDate.toString("yyyy-MM-dd"));

            if (dbAmount != null) {
                total = dbAmount;
            }
            topView.setPaidThisMonth(total);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private void populatePaidToday() {
        try {
            Double total = 0.0;
            DateTime today = new DateTime();
            Double dbTotal = paymentRepository.getTotalAmountPaidByDate(today.toString("yyyy-MM-dd"));
            if (dbTotal != null) {
                total = dbTotal;
            }
            topView.setPaidToday(total);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        //populate not allocated
        try {
            Double notAllocated = 0d;
            //get mpesa transactions not allocated
            notAllocated = mpesaRepository.findSumAllocated(0);
            topView.setNotAllocated(notAllocated);
        } catch (Exception ex) {

        }
    }

    private void populatePaidLastMonth() {
        try {
            Double total = 0.0;
            BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);
            if (billingMonth == null) {
                return;
            }
            Double dbTotal = billRepository.getTotalAmountByBillingMonth(billingMonth.getBillingMonthId());
            if (dbTotal != null) {
                total = dbTotal;
            }
            topView.setPaidLastMonth(total);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private Integer getMonths() {
        Integer months = 12;
        try {
            months = Integer.valueOf(optionService.getOption("GRAPH_PERIOD_IN_MONTHS").getValue());
            months = Math.abs(months);
        } catch (Exception ex) {

        }
        months *= -1;
        return months;
    }

    private Calendar getFirstDayThisMonth() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DATE, 1);
        return today;
    }

    private void populateXaxis() {
        try {
            XAxisMeta xAxisMeta = new XAxisMeta();
            xAxisMeta.setName("categories");
            List<String> items = new ArrayList<>();

            //start date should be less 2 years
            DateTime startDate = new DateTime().dayOfMonth().withMinimumValue().plusMonths(this.getMonths());
            while (startDate.isBeforeNow()) {
                items.add(startDate.toString("MMM yyyy"));
                startDate = startDate.plusMonths(1);
            }
            xAxisMeta.setItems(items);
            billsPaymentsLineGraph.setxAxisMeta(xAxisMeta);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }

    }

    private void populateXZonesBarGraph() {
        try {
            XAxisMeta xAxisMeta = new XAxisMeta();
            xAxisMeta.setName("categories");
            List<String> items = new ArrayList<>();

            List<Zone> zones = zoneRepository.findAll();
            if (zones.isEmpty()) {
                return;
            }
            for (Zone zone : zones) {
                items.add(zone.getName().trim());
            }
            xAxisMeta.setItems(items);
            //set x axis for zone bar graph
            zonesBarGraph.setxAxisMeta(xAxisMeta);

            //set x axis for zone balances
            zoneBalances.setxAxisMeta(xAxisMeta);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void populateZonesAccountBalancesBarGraph() {
        try {
            List<GraphSeries> seriesList = new ArrayList<>();
            //payments
            List<GraphSeries> series = this.getZoneAccountBalancesGraphData();
            if (!series.isEmpty()) {
                seriesList.addAll(series);
            }
            zoneBalances.setSeries(seriesList);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void populateZonesBarGraph() {
        try {
            List<GraphSeries> seriesList = new ArrayList<>();
            //payments
            List<GraphSeries> series = this.getZonesBarGraphDataPayments();
            if (!series.isEmpty()) {
                seriesList.addAll(series);
            }

            //bills
            series = this.getZonesBillsBarGraphData();
            if (!series.isEmpty()) {
                seriesList.addAll(series);
            }
            zonesBarGraph.setSeries(seriesList);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void populateBillsPaymentsLineGraph() {
        try {
            List<GraphSeries> seriesList = new ArrayList<>();

            //add payments
            List<GraphSeries> paymentTypesList = this.getPaymentTypesData();

            if (!paymentTypesList.isEmpty()) {
                seriesList.addAll(paymentTypesList);
            }

            //add billed and meter rent
            List<GraphSeries> billedMeterRentList = this.getBilledMeterRentData();
            if (!billedMeterRentList.isEmpty()) {
                seriesList.addAll(billedMeterRentList);
            }

            //Other charges
            List<GraphSeries> otherChargesList = this.getOtherChargesData();
            if (!otherChargesList.isEmpty()) {
                seriesList.addAll(otherChargesList);
            }
            billsPaymentsLineGraph.setSeries(seriesList);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Transactional
    private List<GraphSeries> getZonesBillsBarGraphData() {
        List<GraphSeries> seriesList = new ArrayList<>();
        GraphSeries billSeries = new GraphSeries();
        billSeries.setName("Bills");
        List<Double> billValues = new ArrayList<>();
        List<Zone> zones = zoneRepository.findAll();
        DateTime today = new DateTime().dayOfMonth().withMaximumValue();
        DateTime fromDate = new DateTime().dayOfMonth().withMinimumValue().plusMonths(this.getMonths());

        if (!zones.isEmpty()) {
            for (Zone zone : zones) {
                Double total = 0.0;
                Double dbTotal = billRepository.getTotalAmountByZoneByBillingMonth(zone.getZoneId(), fromDate.toString("yyyy-MM-dd"), today.toString("yyyy-MM-dd"));
                //amountBilled = billRepository.findByContent(zone.getName(), format1.format(fromDate.getTime()).toString(), format1.format(today.getTime()).toString());
                if (dbTotal != null) {
                    total = dbTotal;
                }
                billValues.add(total);
            }
            billSeries.setData(billValues);
            seriesList.add(billSeries);
        }

        return seriesList;
    }

    @Transactional
    private List<GraphSeries> getZoneAccountBalancesGraphData() {
        List<GraphSeries> seriesList = new ArrayList<>();
        GraphSeries activeSeries = new GraphSeries();
        GraphSeries inactiveSeries = new GraphSeries();
        GraphSeries allSeries = new GraphSeries();

        //set series names
        activeSeries.setName("Active Account Balances");
        inactiveSeries.setName("Inactive Account Balances");
        allSeries.setName("All Account Balances");

        List<Double> activeValues = new ArrayList<>();
        List<Double> inActiveValues = new ArrayList<>();
        List<Double> allValues = new ArrayList<>();

        //List<Double> billValues = new ArrayList<>();

        List<Zone> zones = zoneRepository.findAll();
        if (!zones.isEmpty()) {
            for (Zone zone : zones) {
                //Get accounts by zone
                Double activeBalances = 0d;
                Double inactiveBalances = 0d;
                Double allBal = 0d;

                Double dbActiveBalances = accountRepository.getBalancesByStatusByZone(zone.getZoneId(), 1);
                Double dbInActiveBalances = accountRepository.getBalancesByStatusByZone(zone.getZoneId(), 0);

                if (dbActiveBalances != null) {
                    activeBalances = dbActiveBalances;
                }

                if (dbInActiveBalances != null) {
                    inactiveBalances = dbInActiveBalances;
                }

                //add bill series values
                inActiveValues.add(inactiveBalances);
                activeValues.add(activeBalances);
                allValues.add(inactiveBalances + activeBalances);
            }

            activeSeries.setData(activeValues);
            inactiveSeries.setData(inActiveValues);
            allSeries.setData(allValues);

            seriesList.add(activeSeries);
            seriesList.add(inactiveSeries);
            seriesList.add(allSeries);
        }
        return seriesList;
    }

    @Transactional
    private List<GraphSeries> getZonesBarGraphDataPayments() {
        List<GraphSeries> seriesList = new ArrayList<>();
        GraphSeries billSeries = new GraphSeries();
        billSeries.setName("Payments");
        List<Double> paymentValues = new ArrayList<>();
        DateTime today = new DateTime().withTimeAtStartOfDay().dayOfMonth().withMaximumValue();
        DateTime fromDate = new DateTime().withTimeAtStartOfDay().dayOfMonth().withMinimumValue().plusMonths(this.getMonths());

        List<Zone> zones = zoneRepository.findAll();
        if (!zones.isEmpty()) {
            for (Zone zone : zones) {
                Double total = 0.0;
                Double dbTotal = paymentRepository.getTotalAmountByZoneByDate(zone.getZoneId(), fromDate.toString("yyyy-MM-dd"), today.toString("yyyy-MM-dd"));
                if (dbTotal != null) {
                    total = dbTotal;
                }
                paymentValues.add(total);
            }
            billSeries.setData(paymentValues);
            seriesList.add(billSeries);
        }
        return seriesList;
    }

    @Transactional
    private List<GraphSeries> getPaymentTypesData() {

        List<GraphSeries> seriesList = new ArrayList<>();

        List<PaymentType> paymentTypes = paymentTypeRepository.findAll();

        if (!paymentTypes.isEmpty()) {
            for (PaymentType paymentType : paymentTypes) {
                GraphSeries series = new GraphSeries();
                series.setName(paymentType.getName());
                List<Double> values = new ArrayList<>();
                DateTime toDate = new DateTime().withTimeAtStartOfDay().dayOfMonth().withMaximumValue();
                DateTime fromDate = new DateTime().withTimeAtStartOfDay().dayOfMonth().withMinimumValue().plusMonths(this.getMonths());
                while (fromDate.isBefore(toDate)) {
                    DateTime newToDate = fromDate.plusMonths(1).minusMinutes(1);
                    Double total = 0.0;
                    Double dbTotal = paymentRepository.getTotalAmountByPaymentTypeByDate(paymentType.getPaymentTypeId(), fromDate.toString("yyyy-MM-dd"), newToDate.toString("yyyy-MM-dd"));
                    if (dbTotal != null) {
                        total = dbTotal;
                    }
                    values.add(total);
                    fromDate = fromDate.plusMonths(1);
                }
                series.setData(values);
                seriesList.add(series);
            }
        }

        return seriesList;
    }

    @Transactional
    private List<GraphSeries> getBilledMeterRentData() {
        List<GraphSeries> seriesList = new ArrayList<>();

        //graph details
        GraphSeries billSeries = new GraphSeries();
        billSeries.setName("Water Sale Bills");
        List<Double> billValues = new ArrayList<>();

        GraphSeries meterRentSeries = new GraphSeries();
        meterRentSeries.setName("Meter Rent Bills");
        List<Double> meterRentValues = new ArrayList<>();

        DateTime toDate = new DateTime().withTimeAtStartOfDay().dayOfMonth().withMaximumValue();
        DateTime fromDate = new DateTime().withTimeAtStartOfDay().dayOfMonth().withMinimumValue().plusMonths(this.getMonths());
        while (fromDate.isBefore(toDate)) {
            DateTime bMonth = fromDate.withDayOfMonth(24);
            BillingMonth month = billingMonthRepository.findByMonth(bMonth.toString("yyyy-MM-dd"));

            Double billedTotal = 0d;
            Double meterRentTotal = 0d;

            if (month != null) {
                //Billed Amount
                Double billed = billRepository.getTotalAmountByBillingMonth(month.getBillingMonthId());
                if (billed != null) {
                    billedTotal += billed;
                }

                Double meterRent = billRepository.getTotalMeterRentByBillingMonth(month.getBillingMonthId());
                if (meterRent != null) {
                    meterRentTotal += meterRent;
                }
            }
            billValues.add(billedTotal);
            meterRentValues.add(meterRentTotal);
            fromDate = fromDate.plusMonths(1);
        }

        billSeries.setData(billValues);
        meterRentSeries.setData(meterRentValues);

        seriesList.add(billSeries);
        seriesList.add(meterRentSeries);
        return seriesList;
    }

    @Transactional
    private List<GraphSeries> getOtherChargesData() {
        List<GraphSeries> seriesList = new ArrayList<>();
        List<BillItemType> billItemTypes = billItemTypeRepository.findAll();
        if (!billItemTypes.isEmpty()) {
            for (BillItemType billItemType : billItemTypes) {
                //graph details
                GraphSeries series = new GraphSeries();
                series.setName(billItemType.getName());
                List<Double> values = new ArrayList<>();
                DateTime toDate = new DateTime().withTimeAtStartOfDay().dayOfMonth().withMaximumValue();
                DateTime fromDate = new DateTime().withTimeAtStartOfDay().dayOfMonth().withMinimumValue().plusMonths(this.getMonths());
                while (fromDate.isBefore(toDate)) {
                    DateTime bMonth = fromDate.withDayOfMonth(24);
                    BillingMonth month = billingMonthRepository.findByMonth(bMonth.toString("yyyy-MM-dd"));

                    Double total = 0d;
                    if (month != null) {
                        Double dbTotal = billItemTypeRepository.getTotalByTypeByBillingMonth(billItemType.getBillTypeId(), month.getBillingMonthId());
                        if (dbTotal != null) {
                            total = dbTotal;
                        }
                    }
                    values.add(total);
                    fromDate = fromDate.plusMonths(1);
                }
                series.setData(values);
                seriesList.add(series);
            }
        }
        return seriesList;
    }

    public RestResponse getStats(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "dashboard_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                StatsResponse statsResponse = new StatsResponse();
                TopView topView1 = new TopView();
                if (authManager.grant(requestObject.getToken(), "stats_consumer_count").getStatusCode() == HttpStatus.OK) {
                    topView1.setConsumers(this.topView.getConsumers());
                }

                if (authManager.grant(requestObject.getToken(), "stats_accounts_count").getStatusCode() == HttpStatus.OK) {
                    topView1.setAccounts(this.topView.getAccounts());
                }

                if (authManager.grant(requestObject.getToken(), "stats_accounts_active_count").getStatusCode() == HttpStatus.OK) {
                    topView1.setActive(this.topView.getActive());
                }

                if (authManager.grant(requestObject.getToken(), "stats_accounts_inactive_count").getStatusCode() == HttpStatus.OK) {
                    topView1.setInactive(this.topView.getInactive());
                }
                if (authManager.grant(requestObject.getToken(), "stats_billed_this_month").getStatusCode() == HttpStatus.OK) {
                    topView1.setPaidLastMonth(this.topView.getPaidLastMonth());
                }

                if (authManager.grant(requestObject.getToken(), "stats_paid_this_month").getStatusCode() == HttpStatus.OK) {
                    topView1.setPaidThisMonth(this.topView.getPaidThisMonth());
                }

                if (authManager.grant(requestObject.getToken(), "stats_paid_today").getStatusCode() == HttpStatus.OK) {
                    topView1.setPaidToday(this.topView.getPaidToday());
                }

                if (authManager.grant(requestObject.getToken(), "stats_credit_balances").getStatusCode() == HttpStatus.OK) {
                    topView1.setCreditBalances(this.topView.getCreditBalances());
                }

                if (authManager.grant(requestObject.getToken(), "stats_balances").getStatusCode() == HttpStatus.OK) {
                    topView1.setBalances(this.topView.getBalances());
                }

                if (authManager.grant(requestObject.getToken(), "stats_payments_not_allocated").getStatusCode() == HttpStatus.OK) {
                    topView1.setNotAllocated(this.topView.getNotAllocated());
                }

                //statsResponse.setTopView(this.topView);
                if (authManager.grant(requestObject.getToken(), "stats_sms_balance").getStatusCode() == HttpStatus.OK) {
                    topView1.setSmsBalance(smsService.getBalance());
                }
                statsResponse.setTopView(topView1);

                if (authManager.grant(requestObject.getToken(), "stats_bills_payments_linegraph").getStatusCode() == HttpStatus.OK) {
                    statsResponse.setBillsPaymentsLineGraph(billsPaymentsLineGraph);
                }
                if (authManager.grant(requestObject.getToken(), "stats_zones_bargraph").getStatusCode() == HttpStatus.OK) {
                    statsResponse.setZonesBarGraph(zonesBarGraph);
                }

                //zone account balances
                if (authManager.grant(requestObject.getToken(), "stats_zones_account_balances").getStatusCode() == HttpStatus.OK) {
                    statsResponse.setZoneBalances(this.zoneBalances);
                }


                responseObject.setMessage("fetched stats data successfully");
                responseObject.setPayload(statsResponse);
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
