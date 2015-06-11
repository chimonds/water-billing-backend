package ke.co.suncha.simba.aqua.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by manyala on 6/11/15.
 */
@Entity
@Table(name = "postbank_transactions")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PostBankTransaction extends SimbaBaseEntity implements Serializable {
    @Id
    @Column(name = "postbank_transaction_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long postbank_transaction_id;

    @Column(name = "SEQ_NO", unique = true)
    private String seqNo;

    private String ACCT_NO;
    private String TXNDATETIME;
    private String BRANCH;
    private Double BILL_AMT = 0d;
    private Double CHARGED_AMT;
    private Double PAID_AMT;
    private Double RUNNING_BAL;
    private String PAYERACCT;
    private String PAYEE_NAMES;

    @Column(name = "allocated")
    private Integer allocated = 0;

    @Column(name = "notified")
    private Boolean notified = false;

    @Column(name = "assigned")
    private Boolean assigned = false;

    @Column(name = "notes", length = 1000)
    private String notes="";

    @Column(name = "date_assigned")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar dateAssigned= Calendar.getInstance();

    // a payment has a payment type
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    private Account account;

    public long getPostbank_transaction_id() {
        return postbank_transaction_id;
    }

    public void setPostbank_transaction_id(long postbank_transaction_id) {
        this.postbank_transaction_id = postbank_transaction_id;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public String getACCT_NO() {
        return ACCT_NO;
    }

    public void setACCT_NO(String ACCT_NO) {
        this.ACCT_NO = ACCT_NO;
    }

    public String getTXNDATETIME() {
        return TXNDATETIME;
    }

    public void setTXNDATETIME(String TXNDATETIME) {
        this.TXNDATETIME = TXNDATETIME;
    }

    public String getBRANCH() {
        return BRANCH;
    }

    public void setBRANCH(String BRANCH) {
        this.BRANCH = BRANCH;
    }

    public Double getBILL_AMT() {
        return BILL_AMT;
    }

    public void setBILL_AMT(Double BILL_AMT) {
        this.BILL_AMT = BILL_AMT;
    }

    public Double getCHARGED_AMT() {
        return CHARGED_AMT;
    }

    public void setCHARGED_AMT(Double CHARGED_AMT) {
        this.CHARGED_AMT = CHARGED_AMT;
    }

    public Double getPAID_AMT() {
        return PAID_AMT;
    }

    public void setPAID_AMT(Double PAID_AMT) {
        this.PAID_AMT = PAID_AMT;
    }

    public Double getRUNNING_BAL() {
        return RUNNING_BAL;
    }

    public void setRUNNING_BAL(Double RUNNING_BAL) {
        this.RUNNING_BAL = RUNNING_BAL;
    }

    public String getPAYERACCT() {
        return PAYERACCT;
    }

    public void setPAYERACCT(String PAYERACCT) {
        this.PAYERACCT = PAYERACCT;
    }

    public String getPAYEE_NAMES() {
        return PAYEE_NAMES;
    }

    public void setPAYEE_NAMES(String PAYEE_NAMES) {
        this.PAYEE_NAMES = PAYEE_NAMES;
    }

    public Integer getAllocated() {
        return allocated;
    }

    public void setAllocated(Integer allocated) {
        this.allocated = allocated;
    }

    public Boolean getNotified() {
        return notified;
    }

    public void setNotified(Boolean notified) {
        this.notified = notified;
    }

    public Boolean getAssigned() {
        return assigned;
    }

    public void setAssigned(Boolean assigned) {
        this.assigned = assigned;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Calendar getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(Calendar dateAssigned) {
        this.dateAssigned = dateAssigned;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
