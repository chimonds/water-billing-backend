package ke.co.suncha.simba.aqua.scheme.zone.meterReader;

import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.UserService;
import ke.co.suncha.simba.admin.version.ReleaseManager;
import ke.co.suncha.simba.aqua.scheme.zone.ZoneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by maitha.manyala on 8/30/17.
 */
@Service
public class MeterReaderManager {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuthManager authManager;

    @Autowired
    private AuditService auditService;

    @Autowired
    ZoneService zoneService;

    @Autowired
    ReleaseManager releaseManager;

    @Autowired
    UserService userService;

    @Autowired
    MeterReaderService meterReaderService;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    @Transactional
    public RestResponse addMeterReaderToZone(RestRequestObject<MeterReader> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "meterReader_create");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                MeterReader meterReader = requestObject.getObject();
                if (meterReader.getUser() == null || meterReader.getZone() == null) {
                    responseObject.setMessage("Invalid user or zone resource");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else if (userService.getById(meterReader.getUser().getUserId()) == null ||
                        zoneService.getById(meterReader.getZone().getZoneId()) == null) {
                    responseObject.setMessage("Invalid user or zone resource");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else if (meterReaderService.isMeterReaderInZone(meterReader.getUser().getUserId(), meterReader.getZone().getZoneId())) {
                    responseObject.setMessage("User already a meter ready in this zone");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else {
                    meterReader = meterReaderService.addMeterReaderToZone(meterReader.getUser().getUserId(), meterReader.getZone().getZoneId());

                    // package response
                    responseObject.setMessage("Meter reader created successfully. ");
                    responseObject.setPayload(meterReader);
                    response = new RestResponse(responseObject, HttpStatus.CREATED);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(meterReader.getMeterReaderId()));
                    auditRecord.setCurrentData(meterReader.toString());
                    auditRecord.setParentObject("MeterReader");
                    auditRecord.setNotes("CREATED METER_READER");
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
    public RestResponse removeMeterReaderFromZone(RestRequestObject<MeterReader> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "meterReader_remove");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                MeterReader meterReader = requestObject.getObject();
                if (meterReader.getUser() == null || meterReader.getZone() == null) {
                    responseObject.setMessage("Invalid user or zone resource");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else if (userService.getById(meterReader.getUser().getUserId()) == null ||
                        zoneService.getById(meterReader.getZone().getZoneId()) == null) {
                    responseObject.setMessage("Invalid user or zone resource");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else if (!meterReaderService.isMeterReaderInZone(meterReader.getUser().getUserId(), meterReader.getZone().getZoneId())) {
                    responseObject.setMessage("User not mapped to this zone");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else {

                    MeterReader parentRecord = meterReaderService.getMeterReader(meterReader.getUser().getUserId(), meterReader.getZone().getZoneId());
                    Long recordId = parentRecord.getMeterReaderId();

                    meterReaderService.removeMeterReaderFromZone(meterReader.getUser().getUserId(), meterReader.getZone().getZoneId());

                    // package response
                    responseObject.setMessage("Meter reader records updated successfully. ");
                    responseObject.setPayload(meterReader);
                    response = new RestResponse(responseObject, HttpStatus.CREATED);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(recordId));
                    auditRecord.setCurrentData(meterReader.toString());
                    auditRecord.setParentObject("MeterReader");
                    auditRecord.setNotes("REMOVED METER_READER");
                    auditService.log(AuditOperation.DELETED, auditRecord);
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
    public RestResponse getZoneMeterReaders(RestRequestObject<MeterReader> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "zones_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                MeterReader meterReader = requestObject.getObject();
                if (meterReader.getZone() == null) {
                    responseObject.setMessage("Invalid zone resource");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else if (zoneService.getById(meterReader.getZone().getZoneId()) == null) {
                    responseObject.setMessage("zone resource");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else {

                    List<User> userList = meterReaderService.getZoneMeterReaders(meterReader.getZone().getZoneId());

                    responseObject.setMessage("Data fetched successfully. ");
                    responseObject.setPayload(userList);
                    response = new RestResponse(responseObject, HttpStatus.CREATED);
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
    public RestResponse getMeterReadersNotInZone(RestRequestObject<MeterReader> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "meterReader_create");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                MeterReader meterReader = requestObject.getObject();
                if (meterReader.getZone() == null) {
                    responseObject.setMessage("Invalid zone resource");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else if (
                        zoneService.getById(meterReader.getZone().getZoneId()) == null) {
                    responseObject.setMessage("Invalid zone resource");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else {
                    List<User> userList = meterReaderService.getMeterReadersNotInZone(meterReader.getZone().getZoneId());
                    // package response
                    responseObject.setMessage("Records fetched successfully. ");
                    responseObject.setPayload(userList);
                    response = new RestResponse(responseObject, HttpStatus.CREATED);
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
        releaseManager.addPermission("meterReader_create");
        releaseManager.addPermission("meterReader_remove");
    }

}
