package ke.co.suncha.simba.aqua.models;

import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha.manyala on 7/9/15.
 */
@Entity
@Table(name = "account_summary")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountSummary implements Serializable {
    @Id
    @Column(name = "account_summary_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long accountSummaryId;

    @NotNull
    @Column(name = "balance")
    private Double balance = (double) 0;

    @NotNull
    @Column(name = "acc_no", unique = true, length = 20)
    private String accNo;

    @Column(name = "acc_name")
    private String accName;

    @Column(name = "zone")
    private String zone;

    @Column(name = "account_status")
    private String status;

    @Column(name = "notify_client")
    private Boolean notifyClient = true;

    public Boolean isNotifyClient() {
        return notifyClient;
    }

    public void setNotifyClient(Boolean notifyClient) {
        this.notifyClient = notifyClient;
    }

    public long getAccountSummaryId() {
        return accountSummaryId;
    }

    public void setAccountSummaryId(long accountSummaryId) {
        this.accountSummaryId = accountSummaryId;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    public String getAccName() {
        return accName;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
