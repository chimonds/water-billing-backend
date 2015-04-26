package ke.co.suncha.simba.aqua.utils;

import ke.co.suncha.simba.aqua.models.BillItemType;
import ke.co.suncha.simba.aqua.models.BillingMonth;

import java.util.List;

/**
 * Created by manyala on 4/25/15.
 */
public class BillRequest {
    private Integer currentReading;
    private Integer previousReading;
    private List<BillItemType> billItemTypes;


    public Integer getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(Integer currentReading) {
        this.currentReading = currentReading;
    }

    public Integer getPreviousReading() {
        return previousReading;
    }

    public void setPreviousReading(Integer previousReading) {
        this.previousReading = previousReading;
    }

    public List<BillItemType> getBillItemTypes() {
        return billItemTypes;
    }

    public void setBillItemTypes(List<BillItemType> billItemTypes) {
        this.billItemTypes = billItemTypes;
    }
}
