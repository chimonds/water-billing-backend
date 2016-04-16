package ke.co.suncha.simba.aqua.reports;

/**
 * Created by manyala on 4/26/15.
 */
public class BalancesReport {
    private String accNo;
    private String accName;
    private Double balance;
    private String zone;
    private Boolean active;
    private String status;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        if (active) {
            this.status = "Active";
        } else {
            this.status = "Inactive";
        }
        return status;
    }


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

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
