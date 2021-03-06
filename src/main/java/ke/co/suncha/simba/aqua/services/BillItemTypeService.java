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
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.version.ReleaseManager;
import ke.co.suncha.simba.aqua.models.BillItemType;
import ke.co.suncha.simba.aqua.repository.BillItemTypeRepository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Service
public class BillItemTypeService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BillItemTypeRepository billItemTypeRepository;

    @Autowired
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    private AuditService auditService;

    @Autowired
    ReleaseManager releaseManager;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public BillItemTypeService() {
    }

    @Transactional
    public RestResponse create(RestRequestObject<BillItemType> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "bill_item_type_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                BillItemType billItemType = requestObject.getObject();
                if (billItemType == null) {
                    responseObject.setMessage("Bill item type can not be empty.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (StringUtils.isEmpty(billItemType.getName())) {
                    responseObject.setMessage("Bill item type name can not be empty.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (billItemType.getAmount() == null) {
                    responseObject.setMessage("Bill item type amount can not be empty.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                //check name if already exists
                if (billItemTypeRepository.findByName(billItemType.getName()) != null) {
                    responseObject.setMessage("Bill item type name already exists.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (billItemType.getAmount() < 0) {
                    responseObject.setMessage("Invalid amount");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                billItemType = billItemTypeRepository.save(billItemType);

                responseObject.setMessage("Billing item type created successfully");
                responseObject.setPayload(billItemType);
                response = new RestResponse(responseObject, HttpStatus.OK);

                //Start - audit trail
                AuditRecord auditRecord = new AuditRecord();
                auditRecord.setParentID(String.valueOf(billItemType.getBillTypeId()));
                auditRecord.setParentObject("BILLING ITEM TYPE");
                auditRecord.setCurrentData(billItemType.toString());
                auditRecord.setNotes("CREATED BILLING ITEM TYPE");
                auditService.log(AuditOperation.UPDATED, auditRecord);
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
    public RestResponse update(RestRequestObject<BillItemType> requestObject, Long billItemTypeId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "bill_item_type_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                BillItemType billItemType = requestObject.getObject();
                BillItemType dbBillItemType = billItemTypeRepository.findOne(billItemTypeId);
                if (dbBillItemType == null) {
                    responseObject.setMessage("Billing item type not found");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else {
                    if (billItemType.getAmount() == null) {
                        responseObject.setMessage("Amount can not be empty");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    } else if (billItemType.getAmount() < 0) {
                        responseObject.setMessage("Invalid amount");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    } else {
                        dbBillItemType.setAmount(billItemType.getAmount());
                        dbBillItemType.setName(billItemType.getName());
                        dbBillItemType = billItemTypeRepository.save(dbBillItemType);

                        responseObject.setMessage("Billing item type updated successfully");
                        responseObject.setPayload(dbBillItemType);
                        response = new RestResponse(responseObject, HttpStatus.OK);

                        //Start - audit trail
                        AuditRecord auditRecord = new AuditRecord();
                        auditRecord.setParentID(String.valueOf(dbBillItemType.getBillTypeId()));
                        auditRecord.setParentObject("BILLING ITEM TYPE");
                        auditRecord.setCurrentData(dbBillItemType.toString());
                        auditRecord.setNotes("UPDATED BILLING ITEM TYPE");
                        auditService.log(AuditOperation.UPDATED, auditRecord);
                        //End - audit trail
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

    public RestResponse getAll(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "bill_item_type_list");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                List<BillItemType> billItemTypes;
                billItemTypes = billItemTypeRepository.findAll();
                if (!billItemTypes.isEmpty()) {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(billItemTypes);
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

    @PostConstruct
    public void addPermissions() {
        releaseManager.addPermission("bill_item_type_create");
        releaseManager.addPermission("bill_item_type_update");
        releaseManager.addPermission("bill_item_type_list");
    }

}
