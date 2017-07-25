package ke.co.suncha.simba.aqua.billing;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maitha.manyala on 7/25/17.
 */
public class TransferredBill {
    private DateTime transactionDate = new DateTime();
    private Double units = 0d;
    private List<String> content = new ArrayList<>();
    private Double amount = 0d;

    public DateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(DateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Double getUnits() {
        return units;
    }

    public void setUnits(Double units) {
        this.units = units;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
