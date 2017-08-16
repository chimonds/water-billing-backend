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
import ke.co.suncha.simba.aqua.account.scheme.Scheme;
import ke.co.suncha.simba.aqua.account.scheme.SchemeRepository;
import ke.co.suncha.simba.aqua.models.Zone;
import ke.co.suncha.simba.aqua.repository.ZoneRepository;

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
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Service
public class ZoneService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    private AuditService auditService;

    @Autowired
    SchemeRepository schemeRepository;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public ZoneService() {

    }

    public List<Zone> getAll() {
        return zoneRepository.findAll();
    }

    @Transactional
    public RestResponse create(RestRequestObject<Zone> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "zones_create");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                Zone zone = requestObject.getObject();
                Zone z = zoneRepository.findByName(zone.getName());
                if (z != null) {
                    responseObject.setMessage("Zone already exists");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else if (zone.getScheme() == null) {
                    responseObject.setMessage("Zone schema empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                } else if (schemeRepository.findOne(zone.getScheme().getSchemeId()) == null) {
                    responseObject.setMessage("Invalid schema empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                } else {
                    // create resource
                    Zone created = zoneRepository.save(zone);

                    // package response
                    responseObject.setMessage("Zone created successfully. ");
                    responseObject.setPayload(created);
                    response = new RestResponse(responseObject, HttpStatus.CREATED);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(created.getZoneId()));
                    auditRecord.setCurrentData(created.toString());
                    auditRecord.setParentObject("Zones");
                    auditRecord.setNotes("CREATED ZONE");
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
    public RestResponse update(RestRequestObject<Zone> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "zones_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Zone zone = requestObject.getObject();
                Zone z = zoneRepository.findOne(zone.getZoneId());
                if (z == null) {
                    responseObject.setMessage("Zone not found");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else if (zone.getScheme() == null) {
                    responseObject.setMessage("Zone schema empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                } else if (schemeRepository.findOne(zone.getScheme().getSchemeId()) == null) {
                    responseObject.setMessage("Invalid schema empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                } else {
                    // setup resource
                    z.setName(zone.getName());
                    z.setDescription(zone.getDescription());
                    if (zone.getScheme() != null) {
                        z.setScheme(schemeRepository.findOne(zone.getScheme().getSchemeId()));
                    }

                    // save
                    zoneRepository.save(z);
                    responseObject.setMessage("Zone  updated successfully");
                    responseObject.setPayload(z);
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
                response = authManager.grant(requestObject.getToken(), "zones_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest p = requestObject.getObject();

                Page<Zone> page;
                if (p.getFilter().isEmpty()) {
                    page = zoneRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                } else {
                    page = zoneRepository.findByNameContains(p.getFilter(), new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
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

    @Transactional
    public RestResponse getAllByScheme(RestRequestObject<Scheme> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "zones_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Scheme scheme = requestObject.getObject();
                Scheme dbScheme = schemeRepository.findOne(scheme.getSchemeId());
                if (dbScheme == null) {
                    responseObject.setMessage("Invalid zone scheme");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else {
                    List<Zone> zones = zoneRepository.findAllByScheme(scheme);
                    if (zones.isEmpty()) {
                        responseObject.setMessage("Your search did not match any records");
                        response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                    } else {
                        responseObject.setMessage("Fetched data successfully");
                        responseObject.setPayload(zones);
                        response = new RestResponse(responseObject, HttpStatus.OK);
                    }
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
