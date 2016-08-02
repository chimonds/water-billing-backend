package ke.co.suncha.simba.admin.service;

import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.repositories.AuditRecordRepository;
import ke.co.suncha.simba.admin.repositories.SystemActionRepository;
import ke.co.suncha.simba.admin.security.AuthManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
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

    public void log(AuditOperation auditOperation, AuditRecord auditRecord) {
        try {
            if (!StringUtils.containsIgnoreCase("DASHBOARD_VIEW", auditRecord.getNotes()) && !StringUtils.containsIgnoreCase("STATS", auditRecord.getNotes())) {
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
