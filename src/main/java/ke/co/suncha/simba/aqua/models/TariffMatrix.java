package ke.co.suncha.simba.aqua.models;

import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;
import ke.co.suncha.simba.aqua.utils.TariffRateType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 4/24/15.
 */
@Entity
@Table(name = "tariff_matrixes")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TariffMatrix extends SimbaBaseEntity implements Serializable {

    @Id
    @Column(name = "tariff_matrix_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long tariffMatrixId;

    @NotNull
    @Column(name = "minimum")
    private Integer minimum = 0;

    @NotNull
    @Column(name = "maximum")
    private Integer maximum = 0;

    @NotNull
    @Column(name = "amount")
    private Double amount = (double) 0;

    @Column(name = "rate_type", length = 10)
    private String rateType;

    // Bills belong to a billing month
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tariff_id")
    private Tariff tariff;


    public long getTariffMatrixId() {
        return tariffMatrixId;
    }

    public void setTariffMatrixId(long tariffMatrixId) {
        this.tariffMatrixId = tariffMatrixId;
    }

    public Integer getMinimum() {
        return minimum;
    }

    public void setMinimum(Integer minimum) {
        this.minimum = minimum;
    }

    public Integer getMaximum() {
        return maximum;
    }

    public void setMaximum(Integer maximum) {
        this.maximum = maximum;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }


    public Tariff getTariff() {
        return tariff;
    }

    public void setTariff(Tariff tariff) {
        this.tariff = tariff;
    }


    public String getRateType() {
        return rateType;
    }

    public void setRateType(String rateType) {
        this.rateType = rateType;
    }
}
