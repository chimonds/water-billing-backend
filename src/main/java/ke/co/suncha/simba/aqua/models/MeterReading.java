package ke.co.suncha.simba.aqua.models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Calendar;

/**
 * Created by maitha.manyala on 7/26/15.
 */
@Entity
@Table(name = "meter_readings")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MeterReading {
    @Id
    @Column(name = "meter_reading_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long meterReadingId;

    @NotNull
    @Column(name = "referenceCode", unique = true)
    private Integer referenceCode;

    @Column(name = "reading", length = 6)
    private Integer reading;

    @Column(name = "latitude", length = 25)
    private String latitude;

    @Column(name = "longitude", length = 25)
    private String longitude;

    @Column(name = "addedBy")
    private String addedBy;

    @Column(name = "accNo", length = 15)
    private String accNo;

    @Column(name = "billingMonth", length = 10)
    private String billingMonth;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar createdOn = Calendar.getInstance();

    public Calendar getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Calendar createdOn) {
        this.createdOn = createdOn;
    }

    public long getMeterReadingId() {
        return meterReadingId;
    }

    public void setMeterReadingId(long meterReadingId) {
        this.meterReadingId = meterReadingId;
    }

    public Integer getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(Integer referenceCode) {
        this.referenceCode = referenceCode;
    }

    public Integer getReading() {
        return reading;
    }

    public void setReading(Integer reading) {
        this.reading = reading;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    public String getBillingMonth() {
        return billingMonth;
    }

    public void setBillingMonth(String billingMonth) {
        this.billingMonth = billingMonth;
    }
}
