package ke.co.suncha.simba.aqua.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.aqua.account.Account;
import org.apache.commons.codec.binary.Base64;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by maitha.manyala on 7/26/15.
 */
@Entity
@Table(name = "meter_readings_mobile")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MeterReading {
    @Id
    @Column(name = "meter_reading_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long meterReadingId;

    @Column(name = "current_reading", length = 6)
    private Double currentReading;

    @Column(name = "previous_reading", length = 6)
    private Double previousReading;

    @Column(name = "latitude", length = 25)
    private String latitude;

    @Column(name = "longitude", length = 25)
    private String longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "billing_month_id")
    private BillingMonth billingMonth;

    @Column(name = "billed")
    private Boolean billed = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "read_by_id")
    private User readBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billed_by_id")
    private User billedBy;

    @JsonIgnore
    @Column(name = "image_path")
    private String imagePath;

    @Transient
    private String imageString;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "read_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime readOn = new DateTime();

    @Column(name = "units_consumed")
    private Double unitsConsumed;

    @Column(name = "units_billed")
    private Double unitsBilled;

    @Column(name = "amount_billed")
    private Double amountBilled;

    @Column(name = "consumption_type")
    private String consumptionType;

    public Double getAmountBilled() {
        return amountBilled;
    }

    public void setAmountBilled(Double amountBilled) {
        this.amountBilled = amountBilled;
    }

    public Double getUnitsConsumed() {
        return unitsConsumed;
    }

    public void setUnitsConsumed(Double unitsConsumed) {
        this.unitsConsumed = unitsConsumed;
    }

    public Double getUnitsBilled() {
        return unitsBilled;
    }

    public void setUnitsBilled(Double unitsBilled) {
        this.unitsBilled = unitsBilled;
    }

    public String getConsumptionType() {
        return consumptionType;
    }

    public void setConsumptionType(String consumptionType) {
        this.consumptionType = consumptionType;
    }

    public long getMeterReadingId() {
        return meterReadingId;
    }

    public void setMeterReadingId(long meterReadingId) {
        this.meterReadingId = meterReadingId;
    }

    public Double getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(Double currentReading) {
        this.currentReading = currentReading;
    }

    public Double getPreviousReading() {
        return previousReading;
    }

    public void setPreviousReading(Double previousReading) {
        this.previousReading = previousReading;
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BillingMonth getBillingMonth() {
        return billingMonth;
    }

    public void setBillingMonth(BillingMonth billingMonth) {
        this.billingMonth = billingMonth;
    }

    public Boolean getBilled() {
        return billed;
    }

    public void setBilled(Boolean billed) {
        this.billed = billed;
    }

    public User getReadBy() {
        return readBy;
    }

    public void setReadBy(User readBy) {
        this.readBy = readBy;
    }

    public User getBilledBy() {
        return billedBy;
    }

    public void setBilledBy(User billedBy) {
        this.billedBy = billedBy;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public DateTime getReadOn() {
        return readOn;
    }

    public void setReadOn(DateTime readOn) {
        this.readOn = readOn;
    }

    public String getImageString() {
        try {
            File file = new File(this.imagePath);
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            imageString = new String(Base64.encodeBase64(bytes), "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return imageString;
    }

    public void setImageString(String imageString) {
        this.imageString = imageString;
    }
}
