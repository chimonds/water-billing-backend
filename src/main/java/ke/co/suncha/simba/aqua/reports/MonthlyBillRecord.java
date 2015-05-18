package ke.co.suncha.simba.aqua.reports;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by manyala on 5/10/15.
 */
public class MonthlyBillRecord extends BaseRecord {
    private String location;
    private String meterSize;
    private String meterNo;
    private Date billingMonth;
    private Integer currentReading;
    private Integer PreviousReading;
    private Integer unitsBilled;
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
    private Boolean inArreas=false;

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

    public Date getBillingMonth() {
        return billingMonth;
    }

    public void setBillingMonth(Date billingMonth) {
        this.billingMonth = billingMonth;
    }

    public Integer getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(Integer currentReading) {
        this.currentReading = currentReading;
    }

    public Integer getPreviousReading() {
        return PreviousReading;
    }

    public void setPreviousReading(Integer previousReading) {
        PreviousReading = previousReading;
    }

    public Integer getUnitsBilled() {
        return unitsBilled;
    }

    public void setUnitsBilled(Integer unitsBilled) {
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
}
