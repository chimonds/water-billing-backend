package ke.co.suncha.simba.aqua.reports.scheduled;

import ke.co.suncha.simba.aqua.account.OnStatus;
import ke.co.suncha.simba.aqua.scheme.Scheme;
import ke.co.suncha.simba.aqua.scheme.zone.Zone;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha.manyala on 8/1/16.
 */
@Entity
@Table(name = "report_headers")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ReportHeader implements Serializable {
    @Id
    @Column(name = "report_header_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long reportHeaderId;

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "time_taken")
    private String timeTaken = "";

    @Column(name = "ymcode")
    private Integer code = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "to_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime toDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id")
    private Scheme scheme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Transient
    private Long billingMonthId;

    @Column(name = "status")
    private Integer status = ReportStatus.PENDING;

    @Column(name = "cut_off")
    private Integer cutOff = CutOffStatus.ALL;

    @Column(name = "on_status")
    private Integer onStatus = OnStatus.TURNED_ON;

    @Column(name = "report_type", nullable = false)
    private Integer reportType;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = Boolean.FALSE;

    public Long getReportHeaderId() {
        return reportHeaderId;
    }

    public void setReportHeaderId(Long reportHeaderId) {
        this.reportHeaderId = reportHeaderId;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public DateTime getToDate() {
        return toDate;
    }

    public void setToDate(DateTime toDate) {
        this.toDate = toDate;
    }

    public Scheme getScheme() {
        return scheme;
    }

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getReportType() {
        return reportType;
    }

    public void setReportType(Integer reportType) {
        this.reportType = reportType;
    }

    public Integer getCutOff() {
        return cutOff;
    }

    public void setCutOff(Integer cutOff) {
        this.cutOff = cutOff;
    }

    public Integer getOnStatus() {
        return onStatus;
    }

    public void setOnStatus(Integer onStatus) {
        this.onStatus = onStatus;
    }

    public String getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(String timeTaken) {
        this.timeTaken = timeTaken;
    }

    public Long getBillingMonthId() {
        return billingMonthId;
    }

    public void setBillingMonthId(Long billingMonthId) {
        this.billingMonthId = billingMonthId;
    }


    public Boolean getSystem() {
        return isSystem;
    }

    public void setSystem(Boolean system) {
        isSystem = system;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "ReportHeader{" +
                "reportHeaderId=" + reportHeaderId +
                ", requestedBy='" + requestedBy + '\'' +
                ", createdOn=" + createdOn +
                ", toDate=" + toDate +
                ", status=" + status +
                ", cutOff=" + cutOff +
                ", onStatus=" + onStatus +
                ", reportType=" + reportType +
                '}';
    }
}
