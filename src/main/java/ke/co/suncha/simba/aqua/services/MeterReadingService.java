package ke.co.suncha.simba.aqua.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.security.Credential;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.admin.utils.Config;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.repository.*;
import ke.co.suncha.simba.aqua.utils.MobileClientRequest;
import ke.co.suncha.simba.aqua.utils.MobileClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;
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
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Created by maitha.manyala on 7/26/15.
 */
@Service
public class MeterReadingService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MeterRepository meterRepository;

    @Autowired
    private MeterAllocationRepository meterAllocationRepository;

    @Autowired
    private MeterOwnerRepository meterOwnerRepository;

    @Autowired
    private MeterSizeRepository meterSizeRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AuthManager authManager;

    @Autowired
    private MeterReadingRepository meterReadingRepository;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    private SimbaOptionService optionService;


    @Autowired
    private AuditService auditService;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public MeterReadingService() {

    }

    @Scheduled(fixedDelay = 3000)
    public void poolRemoteMeterReadings() {
        try {
            Boolean notifyClient = false;
            try {
                notifyClient = Boolean.parseBoolean(optionService.getOption("REMOTE_METER_READINGS_ENABLE").getValue());
            } catch (Exception ex) {

            }
            if (!notifyClient) {
                return;
            }

            RestTemplate restTemplate = new RestTemplate();

            MobileClientRequest request = new MobileClientRequest();
            Credential credential = new Credential();

            credential.setUsername(optionService.getOption("MOBILE_CLIENT_USERNAME").getValue());
            credential.setPassword(optionService.getOption("MOBILE_CLIENT_KEY").getValue());
            request.setLogin(credential);
            //request.setPayload(accountSummary);

            String url = optionService.getOption("MOBILE_CLIENT_ENDPOINT").getValue() + "/get_meter_readings";
            String jsonResponse = restTemplate.postForObject(url, request, String.class);
            //log.info("Response:" + jsonResponse);
            //2. Convert JSON to Java object
            ObjectMapper mapper = new ObjectMapper();
            try {
                List<MeterReading> meterReadings = mapper.readValue(jsonResponse, new TypeReference<List<MeterReading>>() {
                });

                if (!meterReadings.isEmpty()) {
                    for (MeterReading meterReading : meterReadings) {
                        MeterReading reading = meterReadingRepository.findByReferenceCode(meterReading.getReferenceCode());
                        if (reading == null) {
                            meterReadingRepository.save(meterReading);
                        }

                        //Update remote reading allocated
                        request.setPayload(meterReading);
                        url = optionService.getOption("MOBILE_CLIENT_ENDPOINT").getValue() + "/set_reading_synced";
                        jsonResponse = restTemplate.postForObject(url, request, String.class);
                    }
                }
            } catch (Exception ex) {
                //log.error(ex.getMessage());
            }
        } catch (Exception ex)

        {
            log.error(ex.getMessage());
        }
    }

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "meter_readings_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                RestPageRequest p = requestObject.getObject();

                Page<MeterReading> page;
                if (p.getFilter().isEmpty()) {
                    page = meterReadingRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                } else {
                    page = meterReadingRepository.findByAccNoContains(p.getFilter(),

                            new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                }
                if (page.hasContent()) {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(page);
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
