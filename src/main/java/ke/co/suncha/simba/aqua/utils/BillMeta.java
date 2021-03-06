package ke.co.suncha.simba.aqua.utils;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 4/24/15.
 */
public class BillMeta {
    private Double units = 0.0;
    private Double amount = 0.0;
    private String content = "";
    private Boolean billWaterSale = Boolean.TRUE;

    public Boolean getBillWaterSale() {
        return billWaterSale;
    }

    public void setBillWaterSale(Boolean billWaterSale) {
        this.billWaterSale = billWaterSale;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Double getUnits() {
        return units;
    }

    public void setUnits(Double units) {
        this.units = units;
    }
}
