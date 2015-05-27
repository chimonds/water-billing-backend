package ke.co.suncha.simba.aqua.services;

import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.models.SMS;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.repository.ConsumerRepository;
import ke.co.suncha.simba.aqua.repository.SMSRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.json.*;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import java.util.Calendar;
import java.util.List;

/**
 * Created by manyala on 5/26/15.
 */
@Service
@Scope("singleton")
public class SMSService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private SMSRepository smsRepository;

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

    private Boolean processingSMS = false;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public SMSService() {
    }

    @Scheduled(fixedDelay = 3000)
    private void process() {
        sendSMS();
    }

    private void sendSMS() {
        try {
            List<SMS> smsList = smsRepository.findAllBySend(false);
            if (smsList.isEmpty()) {
                return;
            }
            log.info("Processing:" + smsList.size() + " SMSs");

            String sms_username = optionService.getOption("SMS_USERNAME").getValue();
            String sms_api_key = optionService.getOption("SMS_API_KEY").getValue();
            AfricasTalkingGateway gateway = new AfricasTalkingGateway(sms_username, sms_api_key);

            for (SMS sms : smsList) {
                try {
                    log.info("Sending SMS to:" + sms.getMobileNumber());
                    JSONArray results = gateway.sendMessage(sms.getMobileNumber(), sms.getMessage());
                    JSONObject result = results.getJSONObject(0);
                    String status = result.getString("status");
                    if (status.compareTo("Success") == 0) {
                        sms.setDateSend(Calendar.getInstance());
                        sms.setSend(true);
                        smsRepository.save(sms);
                    } else {
                        log.error("Unable to send SMS to:" + sms.getMobileNumber());
                        //smsRepository.save(sms);
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
