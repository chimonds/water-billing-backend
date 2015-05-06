package ke.co.suncha.simba.aqua.reports;

import java.util.Calendar;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 5/6/15.
 */
public class StatementRecord implements Comparable {
    private Calendar transactionDate;
    private String itemType;
    private String refNo;
    private Double amount;
    private Double runningAmount;


    public Calendar getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Calendar transactionDate) {
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
        Calendar transactionDate = ((StatementRecord) object).getTransactionDate();
        /* For descending order*/
        if (transactionDate.before(this.transactionDate)) return 1;
        if (transactionDate.after(this.transactionDate)) return -1;
        return 0;
    }
}
