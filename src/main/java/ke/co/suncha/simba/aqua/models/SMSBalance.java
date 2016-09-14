package ke.co.suncha.simba.aqua.models;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha.manyala on 9/2/16.
 */
@Entity
@Table(name = "sms_balance")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SMSBalance implements Serializable {
    @Id
    @Column(name = "balance_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long balanceId;

    @Column(name = "amount")
    private Double amount = 0d;

    public long getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(long balanceId) {
        this.balanceId = balanceId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
