package ke.co.suncha.simba.admin.service;

import com.mysema.query.BooleanBuilder;
import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.models.QAuditRecord;
import ke.co.suncha.simba.admin.repositories.AuditRecordRepository;
import ke.co.suncha.simba.admin.repositories.SystemActionRepository;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
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

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> Created on 5/21/15.
 */
@Service
public class AuditService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuditRecordRepository auditRecordRepository;

    @Autowired
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    private SystemActionRepository systemActionRepository;

    @Autowired
    private CurrentUserService currentUserService;


    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "logs_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest p = requestObject.getObject();
                BooleanBuilder builder = new BooleanBuilder();
                if (!p.getFilter().isEmpty()) {
                    builder.or(QAuditRecord.auditRecord.author.containsIgnoreCase(p.getFilter()));
                    builder.or(QAuditRecord.auditRecord.notes.containsIgnoreCase(p.getFilter()));
                }

                Page<AuditRecord> page = auditRecordRepository.findAll(builder, new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(page);
                response = new RestResponse(responseObject, HttpStatus.OK);

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

    public void log(AuditOperation auditOperation, AuditRecord auditRecord) {
        try {
            if (!StringUtils.containsIgnoreCase("DASHBOARD_VIEW", auditRecord.getNotes()) && !StringUtils.containsIgnoreCase("STATS", auditRecord.getNotes()) && !StringUtils.containsIgnoreCase("LOGS_VIEW", auditRecord.getNotes()) ) {
                if (auditOperation != AuditOperation.ACCESSED && auditOperation != AuditOperation.VIEWED) {
                    auditRecord.setAuthor(currentUserService.getCurrent().getEmailAddress());
                    auditRecord.setOperation(auditOperation.name());
                    auditRecordRepository.save(auditRecord);
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }
}
