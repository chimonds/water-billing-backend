package ke.co.suncha.simba.aqua.reports.scheduled.monthly;

import ke.co.suncha.simba.aqua.models.BillingMonth;
import ke.co.suncha.simba.aqua.options.SystemOptionService;
import ke.co.suncha.simba.aqua.reports.scheduled.ReportHeader;
import ke.co.suncha.simba.aqua.reports.scheduled.ReportHeaderService;
import ke.co.suncha.simba.aqua.reports.scheduled.ReportStatus;
import ke.co.suncha.simba.aqua.repository.BillingMonthRepository;
import ke.co.suncha.simba.aqua.services.BillingMonthService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by maitha on 10/29/16.
 */
@Service
public class SystemReportService {
    @Autowired
    SystemReportRepository systemReportRepository;

    @Autowired
    ReportHeaderService reportHeaderService;

    @Autowired
    BillingMonthService billingMonthService;

    @Autowired
    BillingMonthRepository billingMonthRepository;

    @Autowired
    SystemOptionService systemOptionService;

    public SystemReport create(SystemReport systemReport) {
        return systemReportRepository.save(systemReport);
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void processRecords() {
        List<SystemReport> systemReports = systemReportRepository.findAllByStatus(0);
        if (systemReports.isEmpty()) {
            return;
        }

        for (SystemReport systemReport : systemReports) {
            processReport(systemReport);
        }
    }

    @Transactional
    private void processReport(SystemReport systemReport) {
        Boolean proceed = Boolean.TRUE;
        SystemReport sr = systemReportRepository.findOne(systemReport.getReportId());
        ReportHeader ageing = reportHeaderService.getOne(sr.getAgeingHeaderId());
        ReportHeader balances = reportHeaderService.getOne(sr.getBalancesHeaderId());
        if (ageing == null || balances == null) {
            proceed = Boolean.FALSE;
        }

        if (ageing.getStatus() != ReportStatus.PROCESSED || balances.getStatus() != ReportStatus.PROCESSED) {
            proceed = Boolean.FALSE;
        }

        if (proceed) {
            //open new billing month
            BillingMonth billingMonth = billingMonthService.getById(sr.getMonthToOpen());
            if (billingMonth != null) {
                billingMonth.setCurrent(1);
                billingMonthRepository.save(billingMonth);
                sr.setStatus(1);
                sr.setBillingMonthOpened(new DateTime());
                systemReportRepository.save(sr);
            }
        }
    }
}
