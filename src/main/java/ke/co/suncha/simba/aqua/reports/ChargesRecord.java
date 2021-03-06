package ke.co.suncha.simba.aqua.reports;

/**
 * Created by maitha.manyala on 8/13/16.
 */
public class ChargesRecord extends BaseRecord {
    String meterNo = "";
    String meterSize = "";
    String meterOwner = "";
    String accountStatus = "";
    String category = "";
    Double reconnectionFee = 0d;
    Double atOwnersRequestFee = 0d;
    Double changeOfAccountName = 0d;
    Double byPassFee = 0d;
    Double bouncedChequeFee = 0d;
    Double surchargeIrrigationFee = 0d;
    Double surchageMisuseFee = 0d;
    Double meterServicingFee = 0d;
    Double meterRent = 0d;
    Double otherCharges = 0d;
    Double totalBill = 0d;
    Double balanceBroughtForward = 0d;
    Double currentReading = 0d;
    Double previousReading = 0d;
    Double units = 0d;
    String consumption;
    Double average = 0d;
    Double meterRentArrears=0.0;
    Double fineArrears=0.0;
    Double waterSaleArrears=0.0;

    public Double getMeterRentArrears() {
        return meterRentArrears;
    }

    public void setMeterRentArrears(Double meterRentArrears) {
        this.meterRentArrears = meterRentArrears;
    }

    public Double getFineArrears() {
        return fineArrears;
    }

    public void setFineArrears(Double fineArrears) {
        this.fineArrears = fineArrears;
    }

    public Double getWaterSaleArrears() {
        return waterSaleArrears;
    }

    public void setWaterSaleArrears(Double waterSaleArrears) {
        this.waterSaleArrears = waterSaleArrears;
    }

    public Double getBalanceBroughtForward() {
        return balanceBroughtForward;
    }

    public void setBalanceBroughtForward(Double balanceBroughtForward) {
        this.balanceBroughtForward = balanceBroughtForward;
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

    public Double getUnits() {
        return units;
    }

    public void setUnits(Double units) {
        this.units = units;
    }

    public String getConsumption() {
        return consumption;
    }

    public void setConsumption(String consumption) {
        this.consumption = consumption;
    }

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMeterOwner() {
        return meterOwner;
    }

    public void setMeterOwner(String meterOwner) {
        this.meterOwner = meterOwner;
    }

    public Double getOtherCharges() {
        return otherCharges;
    }

    public void setOtherCharges(Double otherCharges) {
        this.otherCharges = otherCharges;
    }

    public Double getTotalBill() {
        return totalBill;
    }

    public void setTotalBill(Double totalBill) {
        this.totalBill = totalBill;
    }

    public Double getMeterRent() {
        return meterRent;
    }

    public void setMeterRent(Double meterRent) {
        this.meterRent = meterRent;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getMeterNo() {
        return meterNo;
    }

    public void setMeterNo(String meterNo) {
        this.meterNo = meterNo;
    }

    public String getMeterSize() {
        return meterSize;
    }

    public void setMeterSize(String meterSize) {
        this.meterSize = meterSize;
    }

    public Double getReconnectionFee() {
        return reconnectionFee;
    }

    public void setReconnectionFee(Double reconnectionFee) {
        this.reconnectionFee = reconnectionFee;
    }

    public Double getAtOwnersRequestFee() {
        return atOwnersRequestFee;
    }

    public void setAtOwnersRequestFee(Double atOwnersRequestFee) {
        this.atOwnersRequestFee = atOwnersRequestFee;
    }

    public Double getChangeOfAccountName() {
        return changeOfAccountName;
    }

    public void setChangeOfAccountName(Double changeOfAccountName) {
        this.changeOfAccountName = changeOfAccountName;
    }

    public Double getByPassFee() {
        return byPassFee;
    }

    public void setByPassFee(Double byPassFee) {
        this.byPassFee = byPassFee;
    }

    public Double getBouncedChequeFee() {
        return bouncedChequeFee;
    }

    public void setBouncedChequeFee(Double bouncedChequeFee) {
        this.bouncedChequeFee = bouncedChequeFee;
    }

    public Double getSurchargeIrrigationFee() {
        return surchargeIrrigationFee;
    }

    public void setSurchargeIrrigationFee(Double surchargeIrrigationFee) {
        this.surchargeIrrigationFee = surchargeIrrigationFee;
    }

    public Double getSurchageMisuseFee() {
        return surchageMisuseFee;
    }

    public void setSurchageMisuseFee(Double surchageMisuseFee) {
        this.surchageMisuseFee = surchageMisuseFee;
    }

    public Double getMeterServicingFee() {
        return meterServicingFee;
    }

    public void setMeterServicingFee(Double meterServicingFee) {
        this.meterServicingFee = meterServicingFee;
    }
}
