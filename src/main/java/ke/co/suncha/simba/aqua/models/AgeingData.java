package ke.co.suncha.simba.aqua.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ke.co.suncha.simba.aqua.reports.scheduled.ReportHeader;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha.manyala on 7/12/16.
 */
@Entity
@Table(name = "ageing_data")
//uniqueConstraints = @UniqueConstraint(columnNames = {"accountId", "userId"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AgeingData implements Serializable {
    @Id
    @Column(name = "record_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long ageingDataId;

    @Column(name = "balance_today")
    private Double balanceToday = 0d;

    @Column(name = "balance_one_month_ago")
    private Double balanceOneMonthsAgo = 0d;

    @Column(name = "balance_two_months_ago")
    private Double balanceTwoMonthsAgo = 0d;

    @Column(name = "balance_three_months_ago")
    private Double balanceThreeMonthsAgo = 0d;

    @Column(name = "balance_four_months_ago")
    private Double balanceFourMonthsAgo = 0d;

    @Column(name = "balance_six_months_ago")
    private Double balanceSixMonthsAgo = 0d;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_header_id")
    private ReportHeader reportHeader;

    public Long getAgeingDataId() {
        return ageingDataId;
    }

    public void setAgeingDataId(Long ageingDataId) {
        this.ageingDataId = ageingDataId;
    }

    public Double getBalanceToday() {
        return balanceToday;
    }

    public void setBalanceToday(Double balanceToday) {
        this.balanceToday = balanceToday;
    }

    public Double getBalanceOneMonthsAgo() {
        return balanceOneMonthsAgo;
    }

    public void setBalanceOneMonthsAgo(Double balanceOneMonthsAgo) {
        this.balanceOneMonthsAgo = balanceOneMonthsAgo;
    }

    public Double getBalanceTwoMonthsAgo() {
        return balanceTwoMonthsAgo;
    }

    public void setBalanceTwoMonthsAgo(Double balanceTwoMonthsAgo) {
        this.balanceTwoMonthsAgo = balanceTwoMonthsAgo;
    }

    public Double getBalanceThreeMonthsAgo() {
        return balanceThreeMonthsAgo;
    }

    public void setBalanceThreeMonthsAgo(Double balanceThreeMonthsAgo) {
        this.balanceThreeMonthsAgo = balanceThreeMonthsAgo;
    }

    public Double getBalanceFourMonthsAgo() {
        return balanceFourMonthsAgo;
    }

    public void setBalanceFourMonthsAgo(Double balanceFourMonthsAgo) {
        this.balanceFourMonthsAgo = balanceFourMonthsAgo;
    }

    public Double getBalanceSixMonthsAgo() {
        return balanceSixMonthsAgo;
    }

    public void setBalanceSixMonthsAgo(Double balanceSixMonthsAgo) {
        this.balanceSixMonthsAgo = balanceSixMonthsAgo;
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
}
