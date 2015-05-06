package ke.co.suncha.simba.aqua.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.models.Payment;
import ke.co.suncha.simba.aqua.reports.PaymentRecord;
import ke.co.suncha.simba.aqua.reports.ReportObject;
import ke.co.suncha.simba.aqua.reports.ReportsParam;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.repository.ConsumerRepository;
import ke.co.suncha.simba.aqua.repository.PaymentRepository;
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

    public RestResponse getPaymentsReport(RestRequestObject<ReportsParam> requestObject) {
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
