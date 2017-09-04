package ke.co.suncha.simba.mobile.upload;

/**
 * Created by maitha.manyala on 9/3/17.
 */
public class MobileMeterReading {
    private Double meterReading;

    private Long readingDate;

    private Long accountId;

    public Double getMeterReading() {
        return meterReading;
    }

    public void setMeterReading(Double meterReading) {
        this.meterReading = meterReading;
    }

    public Long getReadingDate() {
        return readingDate;
    }

    public void setReadingDate(Long readingDate) {
        this.readingDate = readingDate;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
