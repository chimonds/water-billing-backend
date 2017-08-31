package ke.co.suncha.simba.mobile.monthly;

/**
 * Created by maitha.manyala on 8/30/17.
 */
public class MobileBillingMonth {
    private Long billingMonthId;
    private Integer isCurrent = 0;
    private Integer isMeterReading = 0;
    private Long billingMonth;

    public Long getBillingMonthId() {
        return billingMonthId;
    }

    public void setBillingMonthId(Long billingMonthId) {
        this.billingMonthId = billingMonthId;
    }

    public Integer getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Integer isCurrent) {
        this.isCurrent = isCurrent;
    }

    public Integer getIsMeterReading() {
        return isMeterReading;
    }

    public void setIsMeterReading(Integer isMeterReading) {
        this.isMeterReading = isMeterReading;
    }

    public Long getBillingMonth() {
        return billingMonth;
    }

    public void setBillingMonth(Long billingMonth) {
        this.billingMonth = billingMonth;
    }
}
