package ke.co.suncha.simba.aqua.toActivate;

import org.joda.time.DateTime;

/**
 * Created by maitha.manyala on 6/20/17.
 */
public class ToActivateRecord {
    private  String accNo;
    private String accName;
    private String zone;
    private String phoneNo;
    private DateTime paidOn;

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

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public DateTime getPaidOn() {
        return paidOn;
    }

    public void setPaidOn(DateTime paidOn) {
        this.paidOn = paidOn;
    }
}
