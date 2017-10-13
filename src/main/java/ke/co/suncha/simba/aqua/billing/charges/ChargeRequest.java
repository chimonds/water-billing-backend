package ke.co.suncha.simba.aqua.billing.charges;

import ke.co.suncha.simba.aqua.models.BillItemType;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by maitha.manyala on 10/13/17.
 */
public class ChargeRequest {
    private List<BillItemType> billItemTypes;
    private DateTime transactionDate = new DateTime();

    public List<BillItemType> getBillItemTypes() {
        return billItemTypes;
    }

    public void setBillItemTypes(List<BillItemType> billItemTypes) {
        this.billItemTypes = billItemTypes;
    }

    public DateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(DateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}
