package ke.co.suncha.simba.aqua.reports.scheduled.monthly;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by maitha on 10/26/16.
 */
@Entity
@Table(name = "system_reports")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SystemReport {
    @Id
    @Column(name = "system_report_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long reportId;

    @Column(name = "status")
    private Integer status = 0;

    @Column(name = "month_to_open")
    private Long monthToOpen;

    @Column(name = "balances_header_id")
    private Long balancesHeaderId;

    @Column(name = "ageing_header_id")
    private Long ageingHeaderId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "billing_month_opened")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime billingMonthOpened = new DateTime();

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getMonthToOpen() {
        return monthToOpen;
    }

    public void setMonthToOpen(Long monthToOpen) {
        this.monthToOpen = monthToOpen;
    }

    public Long getBalancesHeaderId() {
        return balancesHeaderId;
    }

    public void setBalancesHeaderId(Long balancesHeaderId) {
        this.balancesHeaderId = balancesHeaderId;
    }

    public Long getAgeingHeaderId() {
        return ageingHeaderId;
    }

    public void setAgeingHeaderId(Long ageingHeaderId) {
        this.ageingHeaderId = ageingHeaderId;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public DateTime getBillingMonthOpened() {
        return billingMonthOpened;
    }

    public void setBillingMonthOpened(DateTime billingMonthOpened) {
        this.billingMonthOpened = billingMonthOpened;
    }
}
