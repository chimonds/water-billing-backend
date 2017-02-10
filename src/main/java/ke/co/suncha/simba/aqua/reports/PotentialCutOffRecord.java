package ke.co.suncha.simba.aqua.reports;

import org.joda.time.DateTime;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 5/8/15.
 */
public class PotentialCutOffRecord extends BaseRecord {
    private Double beforeBilling;
    private Double afterBilling;
    private Double billedAmount;
    private DateTime lastBillingMonth;

    public Double getBeforeBilling() {
        return beforeBilling;
    }

    public void setBeforeBilling(Double beforeBilling) {
        this.beforeBilling = beforeBilling;
    }

    public Double getAfterBilling() {
        return afterBilling;
    }

    public void setAfterBilling(Double afterBilling) {
        this.afterBilling = afterBilling;
    }

    public Double getBilledAmount() {
        return billedAmount;
    }

    public void setBilledAmount(Double billedAmount) {
        this.billedAmount = billedAmount;
    }

    public DateTime getLastBillingMonth() {
        return lastBillingMonth;
    }

    public void setLastBillingMonth(DateTime lastBillingMonth) {
        this.lastBillingMonth = lastBillingMonth;
    }
}
