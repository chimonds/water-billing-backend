/*
 * The MIT License
 *
 * Copyright 2015 Maitha Manyala.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ke.co.suncha.simba.aqua.services;

import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.aqua.models.Consumer;
import ke.co.suncha.simba.aqua.repository.ConsumerRepository;

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
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Service
public class ConsumerService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConsumerRepository consumerRepository;

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

    public ConsumerService() {

    }

    @Transactional
    public RestResponse create(RestRequestObject<Consumer> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "consumers_create");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Consumer consumer = requestObject.getObject();
                Consumer co = consumerRepository.findByIdentityNo(consumer.getIdentityNo());
                if (co != null) {
                    responseObject.setMessage("Consumer already exists");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else {
                    // create resource
                    Consumer created = consumerRepository.save(consumer);

                    // package response
                    responseObject.setMessage("Consumer created successfully. ");
                    responseObject.setPayload(created);
                    response = new RestResponse(responseObject, HttpStatus.CREATED);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(created.getConsumerId()));
                    auditRecord.setParentObject("CONSUMERS");
                    auditRecord.setCurrentData(created.toString());
                    auditRecord.setNotes("CREATED CONSUMER");
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
    public RestResponse update(RestRequestObject<Consumer> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "consumers_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                Consumer consumer = requestObject.getObject();
                Consumer co = consumerRepository.findOne(consumer.getConsumerId());
                if (co == null) {
                    responseObject.setMessage("Consumer not found");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else {
                    // setup resource
                    co.setFirstName(consumer.getFirstName());
                    co.setLastName(consumer.getLastName());
                    co.setMiddleName(consumer.getMiddleName());
                    co.setEmailAddress(consumer.getEmailAddress());
                    co.setCity(consumer.getCity());
                    co.setPostalAddress(consumer.getPostalAddress());
                    co.setPostalCode(consumer.getPostalCode());
                    co.setPhoneNumber(consumer.getPhoneNumber());
                    co.setIdentityNo(consumer.getIdentityNo());

                    // save
                    consumerRepository.save(co);
                    responseObject.setMessage("Consumer  updated successfully");
                    responseObject.setPayload(co);
                    response = new RestResponse(responseObject, HttpStatus.OK);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(co.getConsumerId()));
                    auditRecord.setParentObject("CONSUMERS");
                    auditRecord.setCurrentData(co.toString());
                    auditRecord.setPreviousData(consumer.toString());
                    auditRecord.setNotes("UPDATED CONSUMER");
                    auditService.log(AuditOperation.UPDATED, auditRecord);
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

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    public RestResponse getOne(RestRequestObject<RestPageRequest> requestObject, Long id) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "consumers_view_profile");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Consumer consumer = consumerRepository.findOne(id);
                if (consumer != null) {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(consumer);
                    response = new RestResponse(responseObject, HttpStatus.OK);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(consumer.getConsumerId()));
                    auditRecord.setParentObject("CONSUMERS");
                    auditRecord.setCurrentData(consumer.toString());
                    auditRecord.setNotes("VIEW CONSUMER PROFILE");
                    auditService.log(AuditOperation.VIEWED, auditRecord);
                    //End - audit trail

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

    public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "consumers_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                RestPageRequest p = requestObject.getObject();

                Page<Consumer> page;
                if (p.getFilter().isEmpty()) {
                    page = consumerRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                } else {
                    page = consumerRepository.findByFirstNameContainsOrLastNameContainsOrMiddleNameContains(p.getFilter(), p.getFilter(), p.getFilter(),

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