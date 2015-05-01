package ke.co.suncha.simba.aqua.reports;

/**
 * Created by manyala on 5/1/15.
 */
public class AccountRecord {
    private  String accNo;
    private String accName;
    private String zone;
    private String location;
    private  String meterNo;
    private String meterOwner;
    private  Integer average;
    private  Boolean active;


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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMeterNo() {
        return meterNo;
    }

    public void setMeterNo(String meterNo) {
        this.meterNo = meterNo;
    }

    public String getMeterOwner() {
        return meterOwner;
    }

    public void setMeterOwner(String meterOwner) {
        this.meterOwner = meterOwner;
    }

    public Integer getAverage() {
        return average;
    }

    public void setAverage(Integer average) {
        this.average = average;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
