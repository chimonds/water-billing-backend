package ke.co.suncha.simba.aqua.services;

import com.mysema.query.BooleanBuilder;
import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.admin.version.ReleaseManager;
import ke.co.suncha.simba.aqua.models.MeterReading;
import ke.co.suncha.simba.aqua.models.QMeterReading;
import ke.co.suncha.simba.aqua.repository.*;
import ke.co.suncha.simba.aqua.repository.MeterRepository;
import ke.co.suncha.simba.mobile.upload.MeterReadingRecordService;
import ke.co.suncha.simba.mobile.upload.MeterReadingRepository;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
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

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;

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
    ReleaseManager releaseManager;

    @Autowired
    private AuditService auditService;

    @Autowired
    MeterReadingRecordService meterReadingRecordService;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public MeterReadingService() {

    }

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    public RestResponse getPage(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "meter_readings_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                RestPageRequest p = requestObject.getObject();
                BooleanBuilder builder = new BooleanBuilder();
                if (StringUtils.isNotEmpty(p.getFilter())) {
                    builder.and(QMeterReading.meterReading.account.accNo.containsIgnoreCase(p.getFilter()));
                }
                if (p.getAccountId() != null) {
                    builder.and(QMeterReading.meterReading.account.accountId.eq(p.getAccountId()));
                }

                if (p.getZoneId() != null) {
                    builder.and(QMeterReading.meterReading.account.zone.zoneId.eq(p.getZoneId()));
                } else {
                    if (p.getSchemeId() != null) {
                        builder.and(QMeterReading.meterReading.account.zone.scheme.schemeId.eq(p.getSchemeId()));
                    }
                }

                if (p.getUserId() != null) {
                    builder.and(QMeterReading.meterReading.readBy.userId.eq(p.getUserId()));
                }

                if (p.getBillingMonthId() != null) {
                    builder.and(QMeterReading.meterReading.billingMonth.billingMonthId.eq(p.getBillingMonthId()));
                }
                Page<MeterReading> page;
                page = meterReadingRepository.findAll(builder, new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));

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

    public RestResponse getMeterReadingImageString(RestRequestObject<MeterReading> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "meter_readings_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Long meterReadingId = requestObject.getObject().getMeterReadingId();

                if (meterReadingId == null) {
                    responseObject.setMessage("Invalid meter reading resource");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                MeterReading meterReading = meterReadingRepository.findOne(meterReadingId);
                if (meterReading == null) {
                    responseObject.setMessage("Invalid meter reading resource");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }
                String imageString = "";

                try {
                    File file = new File(meterReading.getImagePath());
                    FileInputStream fileInputStreamReader = new FileInputStream(file);
                    byte[] bytes = new byte[(int) file.length()];
                    fileInputStreamReader.read(bytes);
                    imageString = new String(Base64.encodeBase64(bytes), "UTF-8");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(imageString);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @Transactional
    public RestResponse update(RestRequestObject<MeterReading> requestObject, Long meterReadingId) {
        RestResponse response;
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "meter_readings_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                if (meterReadingId == null) {
                    return RestResponse.getExpectationFailed("invalid meter reading resource");
                }

                MeterReading meterReading = meterReadingRecordService.getByMeterReadingId(meterReadingId);
                if (meterReading == null) {
                    return RestResponse.getExpectationFailed("Invalid meter reading resource");
                }

                if (meterReading.getBilled()) {
                    return RestResponse.getExpectationFailed("Sorry we can no complete your request, the account has already been billed");
                }

                if (requestObject.getObject() == null) {
                    return RestResponse.getExpectationFailed("Invalid meter reading resource");
                }

                if (requestObject.getObject().getCurrentReading() == null) {
                    return RestResponse.getExpectationFailed("Current reading can not be empty");
                }
                Double pr = meterReading.getCurrentReading();
                meterReading = meterReadingRecordService.updateCurrentReading(meterReadingId, requestObject.getObject());

                //region Audit trail
                AuditRecord auditRecord = new AuditRecord();
                auditRecord.setParentID(String.valueOf(meterReading.getMeterReadingId()));
                auditRecord.setCurrentData(meterReading.toString());
                auditRecord.setPreviousData(pr + "");
                auditRecord.setParentObject("Meter Readings");
                auditRecord.setNotes("UPDATED Meter Reading");
                auditService.log(AuditOperation.UPDATED, auditRecord);
                //endregion

                return RestResponse.getOk("Meter reading updated", meterReading);
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
        releaseManager.addPermission("meter_readings_view");
        releaseManager.addPermission("meter_readings_update");
    }


}
