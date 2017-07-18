package ke.co.suncha.simba.aqua.models;

import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;
import ke.co.suncha.simba.aqua.account.Account;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by manyala on 6/18/15.
 */
@Entity
@Table(name = "ageing_records")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AgeingRecord extends SimbaBaseEntity implements Serializable {
    @Id
    @Column(name = "ageing_record_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long ageingRecordId;
    private Double above0 = 0d;
    private Double above30 = 0d;
    private Double above60 = 0d;
    private Double above90 = 0d;
    private Double above120 = 0d;
    private Double above180 = 0d;
    private Double balance = 0d;
    private String cutOff;
    private String name;
    private String zone;
    private String accNo;

    public Double getAbove0() {
        return above0;
    }

    public void setAbove0(Double above0) {
        this.above0 = above0;
    }

    // an ageing record has an account
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    public String getCutOff() {
        return cutOff;
    }

    public void setCutOff(String cutOff) {
        this.cutOff = cutOff;
    }

    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAgeingRecordId() {
        return ageingRecordId;
    }

    public void setAgeingRecordId(long ageingRecordId) {
        this.ageingRecordId = ageingRecordId;
    }

    public Double getAbove30() {
        return above30;
    }

    public void setAbove30(Double above30) {
        this.above30 = above30;
    }

    public Double getAbove60() {
        return above60;
    }

    public void setAbove60(Double above60) {
        this.above60 = above60;
    }

    public Double getAbove90() {
        return above90;
    }

    public void setAbove90(Double above90) {
        this.above90 = above90;
    }

    public Double getAbove120() {
        return above120;
    }

    public void setAbove120(Double above120) {
        this.above120 = above120;
    }

    public Double getAbove180() {
        return above180;
    }

    public void setAbove180(Double above180) {
        this.above180 = above180;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
