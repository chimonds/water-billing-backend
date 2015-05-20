package ke.co.suncha.simba.aqua.services;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.repository.*;
import ke.co.suncha.simba.aqua.stats.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import scala.Int;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> Created by manyala on 5/19/15.
 */
@Service
@Scope("singleton")
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
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();
    private Config cfg = new Config();
    private HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
    private TopView topView = new TopView();
    private BillsPaymentsLineGraph billsPaymentsLineGraph = new BillsPaymentsLineGraph();


    @Scheduled(fixedDelay = 5000)
    private void populateAccountsConsumersCount() {
        log.info("Populating consumers & accounts count stats...");
        try {
            topView.setAccounts(accountRepository.count());
            topView.setConsumers(consumerRepository.count());
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        log.info("Top view stats..." + topView.toString());
    }

    @Scheduled(fixedDelay = 5000)
    private void populatePaidThisMonth() {
        try {

            Calendar toDate = this.getFirstDayThisMonth();
            toDate.add(Calendar.MONTH, 1);
            toDate.add(Calendar.DATE, -1);

            Calendar fromDate = this.getFirstDayThisMonth();
            List<Payment> payments = paymentRepository.findByTransactionDateBetween(fromDate, toDate);
            Double total = 0.0;
            if (!payments.isEmpty()) {
                for (Payment payment : payments) {
                    if (payment.getPaymentType().isUnique()) {
                        total += payment.getAmount();
                    }
                }
            }
            topView.setPaidThisMonth(total);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Scheduled(fixedDelay = 5000)
    private void populatePaidLastMonth() {
        try {
            Double total = 0.0;


            Calendar fromDate = this.getFirstDayThisMonth();
            fromDate.add(Calendar.MONTH, -1);

            Calendar toDate = this.getFirstDayThisMonth();
            toDate.add(Calendar.DATE, -1);

            List<Payment> payments = paymentRepository.findByTransactionDateBetween(fromDate, toDate);
            if (!payments.isEmpty()) {
                for (Payment payment : payments) {
                    if (payment.getPaymentType().isUnique()) {
                        total += payment.getAmount();
                    }
                }
            }
            topView.setPaidLastMonth(total);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private Integer getMonths() {
        Integer months = -12;
        try {
            months = Integer.valueOf(optionService.getOption("GRAPH_PERIOD_IN_MONTHS").getValue());
        } catch (Exception ex) {

        }
        return months;
    }

    private Calendar getFirstDayThisMonth() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DATE, 1);
        return today;
    }

    @Scheduled(fixedDelay = 5000)
    private void pupulateXaxis() {
        XAxisMeta xAxisMeta = new XAxisMeta();
        xAxisMeta.setName("categories");
        List<String> items = new ArrayList<>();

        //start date should be less 2 years
        Calendar fromDate = this.getFirstDayThisMonth();
        fromDate.add(Calendar.MONTH, this.getMonths());
        Calendar today = Calendar.getInstance();

        while (fromDate.before(today)) {

            SimpleDateFormat format1 = new SimpleDateFormat("MMM yyyy");
            String formatted = format1.format(fromDate.getTime());
            //log.info("Date:" + formatted);
            items.add(formatted);

            fromDate.add(Calendar.MONTH, 1);
        }
        xAxisMeta.setItems(items);
        billsPaymentsLineGraph.setxAxisMeta(xAxisMeta);
    }

    @Scheduled(fixedDelay = 5000)
    private void populateBillsPaymentsLineGraph() {

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
    }

    private List<GraphSeries> getPaymentTypesData() {

        List<GraphSeries> seriesList = new ArrayList<>();

        List<PaymentType> paymentTypes = paymentTypeRepository.findAll();

        if (!paymentTypes.isEmpty()) {
            for (PaymentType paymentType : paymentTypes) {
                GraphSeries series = new GraphSeries();
                series.setName(paymentType.getName());

                List<Double> values = new ArrayList<>();

                Calendar today = Calendar.getInstance();

                Calendar fromDate = this.getFirstDayThisMonth();
                fromDate.add(Calendar.MONTH, this.getMonths());

                Integer months = this.getMonths();

                while (fromDate.before(today)) {

                    Calendar dFromDate = this.getFirstDayThisMonth();
                    dFromDate.add(Calendar.MONTH, months);

                    months++;

                    Calendar dTodate = this.getFirstDayThisMonth();
                    dTodate.add(Calendar.MONTH, months);
                    dTodate.add(Calendar.DATE, -1);

                    List<Payment> payments = paymentRepository.findByTransactionDateBetweenAndPaymentType(dFromDate, dTodate, paymentType);
                    Double amount = 0.0;
                    if (!payments.isEmpty()) {
                        for (Payment payment : payments) {
                            amount += payment.getAmount();
                        }
                    }
                    values.add(amount);
                    fromDate.add(Calendar.MONTH, 1);
                }

                series.setData(values);
                seriesList.add(series);
            }
        }

        return seriesList;
    }

    private List<GraphSeries> getBilledMeterRentData() {

        List<GraphSeries> seriesList = new ArrayList<>();

        Calendar today = Calendar.getInstance();
        Calendar fromDate = this.getFirstDayThisMonth();
        fromDate.add(Calendar.MONTH, this.getMonths());

        Integer months = this.getMonths();

        //graph details
        GraphSeries billSeries = new GraphSeries();
        billSeries.setName("Bills");
        List<Double> billValues = new ArrayList<>();

        GraphSeries meterRentSeries = new GraphSeries();
        meterRentSeries.setName("Meter Rent");
        List<Double> meterRentValues = new ArrayList<>();


        while (fromDate.before(today)) {
            Calendar billingMonth = Calendar.getInstance();

            billingMonth.set(Calendar.DATE, 24);
            billingMonth.set(Calendar.MONTH, fromDate.get(Calendar.MONTH));
            billingMonth.set(Calendar.YEAR, fromDate.get(Calendar.YEAR));


            BillingMonth month = billingMonthRepository.findByMonth(billingMonth);
            if (month == null) {
                return null;
            }
            Double billed = 0.0;
            Double meterRent = 0.0;
            List<Bill> bills = billRepository.findAllByBillingMonth(month);
            if (!bills.isEmpty()) {
                for (Bill bill : bills) {
                    billed += bill.getAmount();
                    meterRent += bill.getMeterRent();
                }
                billValues.add(billed);
                meterRentValues.add(meterRent);
            }
            fromDate.add(Calendar.MONTH, 1);
        }

        billSeries.setData(billValues);
        meterRentSeries.setData(meterRentValues);

        seriesList.add(billSeries);
        seriesList.add(meterRentSeries);
        return seriesList;
    }

    private List<GraphSeries> getOtherChargesData() {

        List<GraphSeries> seriesList = new ArrayList<>();

        List<BillItemType> billItemTypes = billItemTypeRepository.findAll();

        if (billItemTypes.isEmpty()) {
            return null;
        }


        for (BillItemType billItemType : billItemTypes) {
            //graph details
            GraphSeries series = new GraphSeries();
            series.setName(billItemType.getName());
            List<Double> values = new ArrayList<>();


            Calendar today = Calendar.getInstance();
            Calendar fromDate = this.getFirstDayThisMonth();
            fromDate.add(Calendar.MONTH, this.getMonths());

            Integer months = this.getMonths();
            while (fromDate.before(today)) {
                Calendar billingMonth = Calendar.getInstance();

                billingMonth.set(Calendar.DATE, 24);
                billingMonth.set(Calendar.MONTH, fromDate.get(Calendar.MONTH));
                billingMonth.set(Calendar.YEAR, fromDate.get(Calendar.YEAR));


                BillingMonth month = billingMonthRepository.findByMonth(billingMonth);
                if (month == null) {
                    return null;
                }
                Double amount = 0.0;
                List<Bill> bills = billRepository.findAllByBillingMonth(month);
                if (!bills.isEmpty()) {
                    for (Bill bill : bills) {
                        if (!bill.getBillItems().isEmpty()) {
                            for (BillItem billItem : bill.getBillItems()) {
                                if (billItem.getBillItemType().getName().compareToIgnoreCase(billItemType.getName()) ==0 ) {
                                    amount += billItem.getAmount();
                                }
                            }
                        }
                    }
                    values.add(amount);
                }
                fromDate.add(Calendar.MONTH, 1);
            }
            series.setData(values);
            seriesList.add(series);
        }
        return seriesList;
    }

    public RestResponse getStats(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "stats_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                StatsResponse statsResponse = new StatsResponse();

                if (authManager.grant(requestObject.getToken(), "stats_top").getStatusCode() == HttpStatus.OK) {
                    statsResponse.setTopView(this.topView);
                }

                if (authManager.grant(requestObject.getToken(), "stats_bills_payments_linegraph").getStatusCode() == HttpStatus.OK) {
                    statsResponse.setBillsPaymentsLineGraph(billsPaymentsLineGraph);
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
