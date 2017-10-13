package ke.co.suncha.simba.aqua.billing.charges;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ke.co.suncha.simba.aqua.models.BillItemType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha.manyala on 10/12/17.
 */
@Entity
@Table(name = "charge_items")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ChargeItem implements Serializable {

    @Id
    @Column(name = "charge_item_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long chargeItemId;

    @NotNull
    @Column(name = "amount")
    private Double amount = 0.0;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    @JoinColumn(name = "bill_item_type_id")
    private BillItemType billItemType;

    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_id")
    private Charge charge;

    public long getChargeItemId() {
        return chargeItemId;
    }

    public void setChargeItemId(long chargeItemId) {
        this.chargeItemId = chargeItemId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public BillItemType getBillItemType() {
        return billItemType;
    }

    public void setBillItemType(BillItemType billItemType) {
        this.billItemType = billItemType;
    }

    public Charge getCharge() {
        return charge;
    }

    public void setCharge(Charge charge) {
        this.charge = charge;
    }
}