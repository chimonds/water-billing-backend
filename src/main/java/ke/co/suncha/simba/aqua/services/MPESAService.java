package ke.co.suncha.simba.aqua.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.models.MPESATransaction;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.repository.AccountStatusHistoryRepository;
import ke.co.suncha.simba.aqua.repository.ConsumerRepository;
import ke.co.suncha.simba.aqua.repository.MPESARepository;
import ke.co.suncha.simba.aqua.utils.MPESARequest;
import ke.co.suncha.simba.aqua.utils.MPESAResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by manyala on 6/6/15.
 */
@Service
public class MPESAService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MPESARepository mpesaRepository;

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

    @Autowired
    private AccountStatusHistoryRepository accountStatusHistoryRepository;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public MPESAService() {

    }

    @Scheduled(fixedDelay = 5000)
    private void getTransactions() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            MPESARequest mpesaRequest = new MPESARequest();
            mpesaRequest.setPay_bill(optionService.getOption("MPESA_PAYBILL").getValue());
            mpesaRequest.setKey(optionService.getOption("MPESA_KEY").getValue());

            String url = optionService.getOption("MPESA_TRANSACTIONS_ENDPOINT").getValue();
            String jsonResponse = restTemplate.postForObject(url, mpesaRequest, String.class);

            //2. Convert JSON to Java object
            ObjectMapper mapper = new ObjectMapper();
            MPESAResponse mpesaResponse = mapper.readValue(jsonResponse, MPESAResponse.class);

            log.info("MPESA:" + mpesaResponse.getMessage());
            if (!mpesaResponse.getError()) {
                if (!mpesaResponse.getPayload().isEmpty()) {
                    for (MPESATransaction mpesaTransaction : mpesaResponse.getPayload()) {
                        try {
                            //save transaction
                            MPESATransaction mpesaTransaction1 = mpesaRepository.findByMpesaCode(mpesaTransaction.getMpesa_code());
                            if (mpesaTransaction1 == null) {
                                mpesaTransaction = mpesaRepository.save(mpesaTransaction);
                            }

                            url = optionService.getOption("MPESA_UPDATE_TRANSACTION_ENDPOINT").getValue();
                            mpesaRequest.setRecord_id(mpesaTransaction.getId().toString());
                            jsonResponse = restTemplate.postForObject(url, mpesaRequest, String.class);
                            mpesaResponse = mapper.readValue(jsonResponse, MPESAResponse.class);
                            if (!mpesaResponse.getError()) {
                                mpesaTransaction.setNotified(true);
                                mpesaTransaction = mpesaRepository.save(mpesaTransaction);
                            }

                        } catch (Exception ex) {

                        }
                    }
                }
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }
}
