package ke.co.suncha.simba.aqua.reports;

import java.util.Calendar;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 5/8/15.
 */
public class PotentialCutOffRecord extends BaseRecord {
    private Double beforeBilling;
    private Double afterBilling;
    private Double billedAmount;
    private Calendar lastBillingMonth;

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

    public Calendar getLastBillingMonth() {
        return lastBillingMonth;
    }

    public void setLastBillingMonth(Calendar lastBillingMonth) {
        this.lastBillingMonth = lastBillingMonth;
    }
}
