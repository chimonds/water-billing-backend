package ke.co.suncha.simba.aqua.reports;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by maitha.manyala on 7/29/16.
 */
public class AccountsReportRequest implements Serializable {
    private Integer onStatus;
    private Boolean cutOff;
    private Long schemeId;
    private Long zoneId;
    private Long billingMonthId;
    private Long paymentTypeId;
    private Long paymentSourceId;
    private DateTime fromDate;
    private DateTime toDate;

    public Integer getOnStatus() {
        return onStatus;
    }

    public void setOnStatus(Integer onStatus) {
        this.onStatus = onStatus;
    }

    public Boolean getIsCutOff() {
        return cutOff;
    }

    public void setCutOff(Boolean cutOff) {
        this.cutOff = cutOff;
    }

    public Long getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(Long schemeId) {
        this.schemeId = schemeId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public Long getBillingMonthId() {
        return billingMonthId;
    }

    public void setBillingMonthId(Long billingMonthId) {
        this.billingMonthId = billingMonthId;
    }

    public Boolean getCutOff() {
        return cutOff;
    }

    public Long getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(Long paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public Long getPaymentSourceId() {
        return paymentSourceId;
    }

    public void setPaymentSourceId(Long paymentSourceId) {
        this.paymentSourceId = paymentSourceId;
    }

    public DateTime getFromDate() {
        return fromDate;
    }

    public void setFromDate(DateTime fromDate) {
        this.fromDate = fromDate;
    }

    public DateTime getToDate() {
        return toDate;
    }

    public void setToDate(DateTime toDate) {
        this.toDate = toDate;
    }
}
