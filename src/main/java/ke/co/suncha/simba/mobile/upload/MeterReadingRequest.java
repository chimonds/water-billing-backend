package ke.co.suncha.simba.mobile.upload;

/**
 * Created by maitha.manyala on 9/4/17.
 */
public class MeterReadingRequest {
    private Double reading;
    private String latitude;
    private String longitude;
    private Long accountId;
    private Long billingMonthId;
    private Integer uploaded;
    private Long readOn;

    public Long getReadOn() {
        return readOn;
    }

    public void setReadOn(Long readOn) {
        this.readOn = readOn;
    }

    public Double getReading() {
        return reading;
    }

    public void setReading(Double reading) {
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

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getBillingMonthId() {
        return billingMonthId;
    }

    public void setBillingMonthId(Long billingMonthId) {
        this.billingMonthId = billingMonthId;
    }

    public Integer getUploaded() {
        return uploaded;
    }

    public void setUploaded(Integer uploaded) {
        this.uploaded = uploaded;
    }
}
