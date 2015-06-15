package ke.co.suncha.simba.aqua.services;

import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.repository.ConsumerRepository;
import ke.co.suncha.simba.aqua.repository.SMSRepository;
import ke.co.suncha.simba.aqua.repository.SMSTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by manyala on 6/14/15.
 */
@Service
public class SMSTemplateService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private SMSRepository smsRepository;

    @Autowired
    private SMSTemplateRepository smsTemplateRepository;

    @Autowired
    private SimbaOptionService optionService;

    @Autowired
    private AuthManager authManager;

    @Autowired
    private CounterService counterService;

    @Autowired
    private GaugeService gaugeService;

    @Autowired
    private AuditService auditService;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public SMSTemplateService() {
    }

    @Transactional
    public RestResponse create(RestRequestObject<SMSTemplate> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {

                response = authManager.grant(requestObject.getToken(), "sms_template_create");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                SMSTemplate smsTemplate = requestObject.getObject();
                SMSTemplate template = smsTemplateRepository.findByName(smsTemplate.getName());

                if (template != null) {
                    responseObject.setMessage("SMS template already exists");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (smsTemplate.getMessage().isEmpty()) {
                    responseObject.setMessage("Message can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                // create resource
                SMSTemplate created = smsTemplateRepository.save(smsTemplate);

                // package response
                responseObject.setMessage("SMS template created successfully. ");
                responseObject.setPayload(created);
                response = new RestResponse(responseObject, HttpStatus.CREATED);

                //Start - audit trail
                AuditRecord auditRecord = new AuditRecord();
                auditRecord.setParentID(String.valueOf(created.getSmsTemplateId()));
                auditRecord.setCurrentData(created.toString());
                auditRecord.setParentObject("SMSTemplates");
                auditRecord.setNotes("CREATED SMS Template");
                auditService.log(AuditOperation.CREATED, auditRecord);
                //End - audit trail

            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @Transactional
    public RestResponse update(RestRequestObject<SMSTemplate> requestObject, Long smsTemplateId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "sms_template_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                SMSTemplate smsTemplate = smsTemplateRepository.findOne(smsTemplateId);

                if (smsTemplate == null) {
                    responseObject.setMessage("SMS template not found");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }
                // setup resource
                SMSTemplate template = requestObject.getObject();
                smsTemplate.setMessage(template.getMessage());
                smsTemplate.setNeedsApproval(template.getNeedsApproval());

                // save
                smsTemplateRepository.save(smsTemplate);
                responseObject.setMessage("SMS template  updated successfully");
                responseObject.setPayload(smsTemplate);
                response = new RestResponse(responseObject, HttpStatus.OK);

            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);

            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "sms_template_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest p = requestObject.getObject();

                Page<SMSTemplate> page;
                if (p.getFilter().isEmpty()) {
                    page = smsTemplateRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                } else {
                    page = smsTemplateRepository.findAllByNameContainsOrMessageContains(p.getFilter(), p.getFilter(), new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                }
                if (page.hasContent()) {

                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(page);
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

}
