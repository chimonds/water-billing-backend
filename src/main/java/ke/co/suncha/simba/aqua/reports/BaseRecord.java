package ke.co.suncha.simba.aqua.reports;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 5/6/15.
 */
public class BaseRecord {
    private String accNo;
    private String accName;
    private String zone;
    private Double amount;


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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
