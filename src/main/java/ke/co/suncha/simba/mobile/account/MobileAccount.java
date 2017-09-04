package ke.co.suncha.simba.mobile.account;

import ke.co.suncha.simba.mobile.zone.MZone;
import org.joda.time.DateTime;

/**
 * Created by maitha.manyala on 8/15/17.
 */
public class MobileAccount {
    private Long accountId;
    private String name;
    private Double balance;
    private String accNo;
    private DateTime lastUpdatedOn;
    private MZone zone;
    private Integer active;
    private Integer hasMeter = 0;
    private String phoneNo = "";

    public Integer getHasMeter() {
        return hasMeter;
    }

    public void setHasMeter(Integer hasMeter) {
        this.hasMeter = hasMeter;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    public DateTime getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(DateTime lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public MZone getZone() {
        return zone;
    }

    public void setZone(MZone zone) {
        this.zone = zone;
    }
}