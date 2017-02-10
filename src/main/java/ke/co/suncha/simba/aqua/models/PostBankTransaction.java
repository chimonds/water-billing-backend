package ke.co.suncha.simba.aqua.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;
import ke.co.suncha.simba.aqua.postbank.PostBankFile;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

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
    private long postbankTransactionId;

    @Column(name = "SEQ_NO", unique = true)
    private String seqNo;

    @Column(name = "ACCT_NO")
    private String accNo;

    @Column(name = "TXNDATETIME")
    private String txndatetime;

    @Column(name = "BRANCH")
    private String branch;

    @Column(name = "BILL_AMT")
    private Double billAmount = 0d;

    @Column(name = "CHARGED_AMT")
    private Double chargedAmount;

    @Column(name = "PAID_AMT")
    private Double paidAmount;

    @Column(name = "RUNNING_BAL")
    private Double runningBalance;

    @Column(name = "PAYERACCT")
    private String payersAccount;

    @Column(name = "PAYEE_NAMES")
    private String payeeNames;

    @Column(name = "allocated")
    private Integer allocated = 0;

    @Column(name = "notified")
    private Boolean notified = false;

    @Column(name = "assigned")
    private Boolean assigned = false;


    @Column(name = "account_valid")
    private Boolean accountValid = Boolean.FALSE;

    @Column(name = "receipt_valid")
    private Boolean receiptValid = Boolean.FALSE;


    @Column(name = "notes", length = 1000)
    private String notes = "";

    @Column(name = "date_assigned")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar dateAssigned = Calendar.getInstance();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "trans_date")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime transDate = new DateTime();


    // a payment has a payment type
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    private Account account;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private PostBankFile postBankFile;


    public long getPostbankTransactionId() {
        return postbankTransactionId;
    }

    public void setPostbankTransactionId(long postbankTransactionId) {
        this.postbankTransactionId = postbankTransactionId;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    public String getTxndatetime() {
        return txndatetime;
    }

    public void setTxndatetime(String txndatetime) {
        this.txndatetime = txndatetime;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Double getBillAmount() {
        return billAmount;
    }

    public void setBillAmount(Double billAmount) {
        this.billAmount = billAmount;
    }

    public Double getChargedAmount() {
        return chargedAmount;
    }

    public void setChargedAmount(Double chargedAmount) {
        this.chargedAmount = chargedAmount;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Double getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(Double runningBalance) {
        this.runningBalance = runningBalance;
    }

    public String getPayersAccount() {
        return payersAccount;
    }

    public void setPayersAccount(String payersAccount) {
        this.payersAccount = payersAccount;
    }

    public String getPayeeNames() {
        return payeeNames;
    }

    public void setPayeeNames(String payeeNames) {
        this.payeeNames = payeeNames;
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

    public Boolean getAccountValid() {
        return accountValid;
    }

    public void setAccountValid(Boolean accountValid) {
        this.accountValid = accountValid;
    }

    public Boolean getReceiptValid() {
        return receiptValid;
    }

    public void setReceiptValid(Boolean receiptValid) {
        this.receiptValid = receiptValid;
    }

    public PostBankFile getPostBankFile() {
        return postBankFile;
    }

    public void setPostBankFile(PostBankFile postBankFile) {
        this.postBankFile = postBankFile;
    }

    public DateTime getTransDate() {
        return transDate;
    }

    public void setTransDate(DateTime transDate) {
        this.transDate = transDate;
    }
}
