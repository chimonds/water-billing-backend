package ke.co.suncha.simba.aqua.stats;

/**
 * Created by manyala on 5/19/15.
 */
public class TopView {
    private Long consumers = 0L;
    private Long accounts =  0L;
    private Double paidThisMonth = 0.0;
    private Double paidLastMonth = 0.0;
    private Long active=0L;
    private Long inactive=0L;

    public Long getActive() {
        return active;
    }

    public void setActive(Long active) {
        this.active = active;
    }

    public Long getInactive() {
        return inactive;
    }

    public void setInactive(Long inactive) {
        this.inactive = inactive;
    }

    public Long getConsumers() {
        return consumers;
    }

    public void setConsumers(Long consumers) {
        this.consumers = consumers;
    }

    public Long getAccounts() {
        return accounts;
    }

    public void setAccounts(Long accounts) {
        this.accounts = accounts;
    }

    public Double getPaidThisMonth() {
        return paidThisMonth;
    }

    public void setPaidThisMonth(Double paidThisMonth) {
        this.paidThisMonth = paidThisMonth;
    }

    public Double getPaidLastMonth() {
        return paidLastMonth;
    }

    public void setPaidLastMonth(Double paidLastMonth) {
        this.paidLastMonth = paidLastMonth;
    }

    @Override
    public String toString() {
        return "TopView{" +
                "consumers=" + consumers +
                ", accounts=" + accounts +
                ", paidThisMonth=" + paidThisMonth +
                ", paidLastMonth=" + paidLastMonth +
                ", active=" + active +
                ", inactive=" + inactive +
                '}';
    }
}
