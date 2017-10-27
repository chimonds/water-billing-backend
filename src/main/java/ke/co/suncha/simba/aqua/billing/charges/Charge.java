package ke.co.suncha.simba.aqua.billing.charges;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.models.BillingMonth;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maitha.manyala on 10/12/17.
 */
@Entity
@Table(name = "scheduled_charges", uniqueConstraints = @UniqueConstraint(columnNames = {"billing_month_id", "account_id"}))
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Charge implements Serializable {
    @Id
    @Column(name = "charge_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long chargeId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Column(name = "billed")
    private Boolean billed = Boolean.FALSE;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_billed")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime billedOn = new DateTime();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_month_id", nullable = false)
    private BillingMonth billingMonth;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "charge", fetch = FetchType.EAGER)
    private List<ChargeItem> chargeItems;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User createdBy;

    public Long getChargeId() {
        return chargeId;
    }

    public void setChargeId(Long chargeId) {
        this.chargeId = chargeId;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public Boolean getBilled() {
        return billed;
    }

    public void setBilled(Boolean billed) {
        this.billed = billed;
    }

    public DateTime getBilledOn() {
        return billedOn;
    }

    public void setBilledOn(DateTime billedOn) {
        this.billedOn = billedOn;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BillingMonth getBillingMonth() {
        return billingMonth;
    }

    public void setBillingMonth(BillingMonth billingMonth) {
        this.billingMonth = billingMonth;
    }

    public List<ChargeItem> getChargeItems() {
        if (chargeItems == null) {
            chargeItems = new ArrayList<>();
        }
        return chargeItems;
    }

    public void setChargeItems(List<ChargeItem> chargeItems) {
        this.chargeItems = chargeItems;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}
