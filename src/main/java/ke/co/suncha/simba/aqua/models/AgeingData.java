package ke.co.suncha.simba.aqua.models;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha.manyala on 7/12/16.
 */
@Entity
@Table(name = "ageing_data", uniqueConstraints = @UniqueConstraint(columnNames = {"accountId", "userId"}))
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AgeingData implements Serializable {
    @Id
    @Column(name = "record_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long ageingDataId;
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
    private Long accountId;
    private Long userId;

    public long getAgeingDataId() {
        return ageingDataId;
    }

    public void setAgeingDataId(long ageingDataId) {
        this.ageingDataId = ageingDataId;
    }

    public Double getAbove0() {
        return above0;
    }

    public void setAbove0(Double above0) {
        this.above0 = above0;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
