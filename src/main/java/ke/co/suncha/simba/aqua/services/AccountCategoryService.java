package ke.co.suncha.simba.aqua.services;

import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.aqua.models.AccountCategory;
import ke.co.suncha.simba.aqua.repository.AccountCategoryRepository;
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

import java.util.List;

/**
 * Created by maitha.manyala on 2/22/16.
 */
@Service
public class AccountCategoryService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountCategoryRepository accountCategoryRepository;

    @Autowired
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    private AuditService auditService;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public AccountCategoryService() {

    }

    @Transactional
    public RestResponse create(RestRequestObject<AccountCategory> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "accountCategory_create");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                AccountCategory accountCategory = requestObject.getObject();
                AccountCategory ac = accountCategoryRepository.findByName(accountCategory.getName());
                if (ac != null) {
                    responseObject.setMessage("Account category already exists");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else {
                    // create resource
                    AccountCategory created = accountCategoryRepository.save(accountCategory);
                    // package response
                    responseObject.setMessage("Account category created successfully. ");
                    responseObject.setPayload(created);
                    response = new RestResponse(responseObject, HttpStatus.CREATED);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(created.getCategoryId()));
                    auditRecord.setCurrentData(created.toString());
                    auditRecord.setParentObject("account_categories");
                    auditRecord.setNotes("CREATED ACCOUNT CATEGORY");
                    auditService.log(AuditOperation.CREATED, auditRecord);
                    //End - audit trail
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @Transactional
    public RestResponse update(RestRequestObject<AccountCategory> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "accountCategory_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                AccountCategory accountCategory = requestObject.getObject();
                AccountCategory ac = accountCategoryRepository.findOne(accountCategory.getCategoryId());
                if (ac == null) {
                    responseObject.setMessage("Account category not found");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else {
                    // setup resource
                    ac.setName(accountCategory.getName());
                    ac.setDescription(accountCategory.getDescription());

                    // save
                    ac = accountCategoryRepository.save(ac);
                    responseObject.setMessage("Account category  updated successfully");
                    responseObject.setPayload(ac);
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

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "accountCategory_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest p = requestObject.getObject();

                Page<AccountCategory> page;
                if (p.getFilter().isEmpty()) {
                    page = accountCategoryRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                } else {
                    page = accountCategoryRepository.findByNameContains(p.getFilter(), new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
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

    public RestResponse getAll(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "accountCategory_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                RestPageRequest p = requestObject.getObject();
                List<AccountCategory> page = accountCategoryRepository.findAll();
                if (!page.isEmpty()) {
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
