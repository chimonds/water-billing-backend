package ke.co.suncha.simba.aqua.reports;

import org.joda.time.DateTime;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 5/6/15.
 */
public class StatementRecord implements Comparable {
    private DateTime transactionDate;
    private String itemType;
    private String refNo;
    private Double amount;
    private Double runningAmount;

    public DateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(DateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getRunningAmount() {
        return runningAmount;
    }

    public void setRunningAmount(Double runningAmount) {
        this.runningAmount = runningAmount;
    }

    @Override
    public int compareTo(Object object) {
        DateTime transactionDate = ((StatementRecord) object).getTransactionDate();
        /* For descending order*/
        if (transactionDate.isBefore(this.transactionDate)) return 1;
        if (transactionDate.isAfter(this.transactionDate)) return -1;
        return 0;
    }
}
