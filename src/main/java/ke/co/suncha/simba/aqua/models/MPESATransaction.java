package ke.co.suncha.simba.aqua.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;
import ke.co.suncha.simba.aqua.account.Account;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by manyala on 6/6/15.
 */
@Entity
@Table(name = "mpesa_transactions")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MPESATransaction extends SimbaBaseEntity implements Serializable {
    @Id
    @Column(name = "recid", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long recordId;

    @NotNull
    @Column(name = "id", unique = true)
    private Integer id = 0;


    @Column(name = "orig", length = 50)
    private String orig;

    @Column(name = "dest", length = 50)
    private String dest;

    @Column(name = "tstamp", length = 50)
    private String tstamp;

    @Column(name = "text", length = 50)
    private String text;

    @Column(name = "mpesa_code", length = 50)
    private String mpesacode;

    @Column(name = "mpesa_acc", length = 50)
    private String mpesaacc;

    @Column(name = "mpesa_msisdn", length = 50)
    private String mpesa_msisdn;

    @Column(name = "mpesa_trx_date", length = 50)
    private String mpesa_trx_date;

    @Column(name = "mpesa_trx_time", length = 50)
    private String mpesa_trx_time;

    @Column(name = "mpesa_amt")
    private Double mpesaamt = 0d;

    @Column(name = "mpesa_sender", length = 50)
    private String mpesa_sender;

    @Column(name = "ipadd", length = 20)
    private String ipadd;

    @Column(name = "reqdata", length = 200)
    private String reqdata;

    @Column(name = "customer_id", length = 100)
    private String customer_id;

    @Column(name = "business_number", length = 10)
    private String business_number;

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

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrig() {
        return orig;
    }

    public void setOrig(String orig) {
        this.orig = orig;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getTstamp() {
        return tstamp;
    }

    public void setTstamp(String tstamp) {
        this.tstamp = tstamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMpesacode() {
        return mpesacode;
    }

    public void setMpesacode(String mpesacode) {
        this.mpesacode = mpesacode;
    }

    public String getMpesaacc() {
        return mpesaacc;
    }

    public void setMpesaacc(String mpesaacc) {
        this.mpesaacc = mpesaacc;
    }

    public String getMpesa_msisdn() {
        return mpesa_msisdn;
    }

    public void setMpesa_msisdn(String mpesa_msisdn) {
        this.mpesa_msisdn = mpesa_msisdn;
    }

    public String getMpesa_trx_date() {
        return mpesa_trx_date;
    }

    public void setMpesa_trx_date(String mpesa_trx_date) {
        this.mpesa_trx_date = mpesa_trx_date;
    }

    public String getMpesa_trx_time() {
        return mpesa_trx_time;
    }

    public void setMpesa_trx_time(String mpesa_trx_time) {
        this.mpesa_trx_time = mpesa_trx_time;
    }

    public Double getMpesaamt() {
        return mpesaamt;
    }

    public void setMpesaamt(Double mpesaamt) {
        this.mpesaamt = mpesaamt;
    }

    public String getMpesa_sender() {
        return mpesa_sender;
    }

    public void setMpesa_sender(String mpesa_sender) {
        this.mpesa_sender = mpesa_sender;
    }

    public String getIpadd() {
        return ipadd;
    }

    public void setIpadd(String ipadd) {
        this.ipadd = ipadd;
    }

    public String getReqdata() {
        return reqdata;
    }

    public void setReqdata(String reqdata) {
        this.reqdata = reqdata;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getBusiness_number() {
        return business_number;
    }

    public void setBusiness_number(String business_number) {
        this.business_number = business_number;
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
