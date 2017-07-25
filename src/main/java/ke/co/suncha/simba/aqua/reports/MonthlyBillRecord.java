package ke.co.suncha.simba.aqua.reports;

import ke.co.suncha.simba.aqua.billing.TransferredBill;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by manyala on 5/10/15.
 */
public class MonthlyBillRecord extends BaseRecord {
    private String location;
    private String meterSize;
    private String meterNo;
    private DateTime billingMonth;
    private Double currentReading;
    private Double PreviousReading;
    private Double unitsBilled;
    private String consumptionType;
    private Double balanceBf;
    private Double totalPayments;
    private Double totalCharges;
    private String billContent;
    private Calendar payBefore;
    private Double billedAmount;
    private Double totalBilledAmount;
    private List<String> billSummaryList;
    private List<PaymentRecord> payments;
    private List<ChargeRecord> charges;
    private List<TransferredBill> bills = new ArrayList<>();
    private Boolean hasOtherBills = Boolean.FALSE;
    private Boolean inArreas = false;
    private Double otherBillsTotal = 0.0;

    public Boolean getInArreas() {
        return inArreas;
    }

    public void setInArreas(Boolean inArreas) {
        this.inArreas = inArreas;
    }

    public List<String> getBillSummaryList() {
        return billSummaryList;
    }

    public void setBillSummaryList(List<String> billSummaryList) {
        this.billSummaryList = billSummaryList;
    }

    public Double getTotalBilledAmount() {
        return totalBilledAmount;
    }

    public void setTotalBilledAmount(Double totalBilledAmount) {
        this.totalBilledAmount = totalBilledAmount;
    }

    public Double getBilledAmount() {
        return billedAmount;
    }

    public void setBilledAmount(Double billedAmount) {
        this.billedAmount = billedAmount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMeterSize() {
        return meterSize;
    }

    public void setMeterSize(String meterSize) {
        this.meterSize = meterSize;
    }

    public String getMeterNo() {
        return meterNo;
    }

    public void setMeterNo(String meterNo) {
        this.meterNo = meterNo;
    }

    public DateTime getBillingMonth() {
        return billingMonth;
    }

    public void setBillingMonth(DateTime billingMonth) {
        this.billingMonth = billingMonth;
    }

    public Double getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(Double currentReading) {
        this.currentReading = currentReading;
    }

    public Double getPreviousReading() {
        return PreviousReading;
    }

    public void setPreviousReading(Double previousReading) {
        PreviousReading = previousReading;
    }

    public Double getUnitsBilled() {
        return unitsBilled;
    }

    public void setUnitsBilled(Double unitsBilled) {
        this.unitsBilled = unitsBilled;
    }

    public String getConsumptionType() {
        return consumptionType;
    }

    public void setConsumptionType(String consumptionType) {
        this.consumptionType = consumptionType;
    }

    public Double getBalanceBf() {
        return balanceBf;
    }

    public void setBalanceBf(Double balanceBf) {
        this.balanceBf = balanceBf;
    }

    public Double getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(Double totalPayments) {
        this.totalPayments = totalPayments;
    }

    public Double getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(Double totalCharges) {
        this.totalCharges = totalCharges;
    }

    public String getBillContent() {
        return billContent;
    }

    public void setBillContent(String billContent) {
        this.billContent = billContent;
    }

    public Calendar getPayBefore() {
        return payBefore;
    }

    public void setPayBefore(Calendar payBefore) {
        this.payBefore = payBefore;
    }

    public List<PaymentRecord> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentRecord> payments) {
        this.payments = payments;
    }

    public List<ChargeRecord> getCharges() {
        return charges;
    }

    public void setCharges(List<ChargeRecord> charges) {
        this.charges = charges;
    }

    public List<TransferredBill> getBills() {
        return bills;
    }

    public void setBills(List<TransferredBill> bills) {
        this.bills = bills;
    }

    public Boolean getHasOtherBills() {
        return hasOtherBills;
    }

    public void setHasOtherBills(Boolean hasOtherBills) {
        this.hasOtherBills = hasOtherBills;
    }

    public Double getOtherBillsTotal() {
        return otherBillsTotal;
    }

    public void setOtherBillsTotal(Double otherBillsTotal) {
        this.otherBillsTotal = otherBillsTotal;
    }
}
