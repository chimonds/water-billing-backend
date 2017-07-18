package ke.co.suncha.simba.aqua.models;

import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;
import ke.co.suncha.simba.aqua.account.Account;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by manyala on 6/2/15.
 */
@Entity
@Table(name = "account_status_history")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountStatusHistory extends SimbaBaseEntity implements Serializable {
    @Id
    @Column(name = "account_status_history_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long accountStatusHistoryId;

    @Column(name = "status_type", length = 15)
    private String statusType;

    @Column(name = "notes", length = 1000)
    private String notes = "";

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public long getAccountStatusHistoryId() {
        return accountStatusHistoryId;
    }

    public void setAccountStatusHistoryId(long accountStatusHistoryId) {
        this.accountStatusHistoryId = accountStatusHistoryId;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }
}
