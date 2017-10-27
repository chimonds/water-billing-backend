package ke.co.suncha.simba.aqua.utils;

import ke.co.suncha.simba.aqua.models.BillItemType;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manyala on 4/25/15.
 */
public class BillRequest {
    private Double currentReading;
    private Double previousReading;
    private List<BillItemType> billItemTypes;
    private Boolean billWaterSale = Boolean.TRUE;
    private DateTime transactionDate = new DateTime();

    public DateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(DateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Double getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(Double currentReading) {
        this.currentReading = currentReading;
    }

    public Double getPreviousReading() {
        return previousReading;
    }

    public void setPreviousReading(Double previousReading) {
        this.previousReading = previousReading;
    }

    public List<BillItemType> getBillItemTypes() {
        if (billItemTypes == null) {
            billItemTypes = new ArrayList<>();
        }
        return billItemTypes;
    }

    public void setBillItemTypes(List<BillItemType> billItemTypes) {
        this.billItemTypes = billItemTypes;
    }

    public Boolean getBillWaterSale() {
        return billWaterSale;
    }

    public void setBillWaterSale(Boolean billWaterSale) {
        this.billWaterSale = billWaterSale;
    }
}
