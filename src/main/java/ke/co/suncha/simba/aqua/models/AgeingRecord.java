package ke.co.suncha.simba.aqua.models;

/**
 * Created by manyala on 6/18/15.
 */
public class AgeingRecord {
    private Double above30 = 0d;
    private Double above60 = 0d;
    private Double above90 = 0d;
    private Double above120 = 0d;
    private Double above180 = 0d;
    private Double over180 = 0d;
    private Double balance = 0d;

    public Double getAbove120() {
        return above120;
    }

    public void setAbove120(Double above120) {
        this.above120 = above120;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public Double getAbove30() {
        return above30;
    }

    public void setAbove30(Double above30) {
        this.above30 = above30;
    }

    public Double getAbove60() {
        return above60;
    }

    public void setAbove60(Double above60) {
        this.above60 = above60;
    }

    public Double getAbove90() {
        return above90;
    }

    public void setAbove90(Double above90) {
        this.above90 = above90;
    }

    public Double getAbove180() {
        return above180;
    }

    public void setAbove180(Double above180) {
        this.above180 = above180;
    }

    public Double getOver180() {
        return over180;
    }

    public void setOver180(Double over180) {
        this.over180 = over180;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
