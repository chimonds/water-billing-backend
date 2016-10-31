package ke.co.suncha.simba.aqua.reports.scheduled;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha.manyala on 8/1/16.
 */
@Entity
@Table(name = "account_balance_records", uniqueConstraints = @UniqueConstraint(columnNames = {"report_header_id", "account_id"}))
//uniqueConstraints = @UniqueConstraint(columnNames = {"accountId", "userId"})
@XmlRootElement
public class AccountBalanceRecord implements Serializable {
    @Id
    @Column(name = "account_balance_record_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long accountBalanceRecordId;

    @Column(name = "balance")
    private Double balance = 0d;

    @Column(name = "cut_off")
    private String cutOff;

    @Column(name = "name")
    private String name;

    @Column(name = "zone")
    private String zone;

    @Column(name = "acc_no")
    private String accNo;

    @JsonIgnore
    @Column(name = "account_id")
    private Long accountId;

    @JsonIgnore
    @Column(name = "scheme_id")
    private Long schemeId;

    @JsonIgnore
    @Column(name = "zone_id")
    private Long zoneId;

    @JsonIgnore
    @Column(name = "is_metered")
    private Boolean metered=Boolean.FALSE;

    @Column(name = "meter_no")
    private String meterNo;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_header_id")
    private ReportHeader reportHeader;

    public Long getAccountBalanceRecordId() {
        return accountBalanceRecordId;
    }

    public void setAccountBalanceRecordId(Long accountBalanceRecordId) {
        this.accountBalanceRecordId = accountBalanceRecordId;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getCutOff() {
        return cutOff;
    }

    public void setCutOff(String cutOff) {
        this.cutOff = cutOff;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public ReportHeader getReportHeader() {
        return reportHeader;
    }

    public void setReportHeader(ReportHeader reportHeader) {
        this.reportHeader = reportHeader;
    }

    public Long getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(Long schemeId) {
        this.schemeId = schemeId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public Boolean getMetered() {
        return metered;
    }

    public void setMetered(Boolean metered) {
        this.metered = metered;
    }

    public String getMeterNo() {
        return meterNo;
    }

    public void setMeterNo(String meterNo) {
        this.meterNo = meterNo;
    }
}
