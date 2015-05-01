package ke.co.suncha.simba.aqua.reports;

/**
 * Created by manyala on 5/1/15.
 */
public class BillRecord {
    private String accNo;
    private String accName;
    private String zone;
    private Integer currentReading;
    private Integer previousReading;
    private Integer units;
    private String consumption;
    private Integer average;
    private Double amountBilled;
    private Double meterRent;
    private Double otherCharges;


    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    public String getAccName() {
        return accName;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

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

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }

    public String getConsumption() {
        return consumption;
    }

    public void setConsumption(String consumption) {
        this.consumption = consumption;
    }

    public Integer getAverage() {
        return average;
    }

    public void setAverage(Integer average) {
        this.average = average;
    }

    public Double getAmountBilled() {
        return amountBilled;
    }

    public void setAmountBilled(Double amountBilled) {
        this.amountBilled = amountBilled;
    }

    public Double getMeterRent() {
        return meterRent;
    }

    public void setMeterRent(Double meterRent) {
        this.meterRent = meterRent;
    }

    public Double getOtherCharges() {
        return otherCharges;
    }

    public void setOtherCharges(Double otherCharges) {
        this.otherCharges = otherCharges;
    }
}
