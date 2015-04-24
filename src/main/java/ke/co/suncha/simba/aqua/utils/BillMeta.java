package ke.co.suncha.simba.aqua.utils;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 4/24/15.
 */
public class BillMeta {
    private Integer units =0;
    private Double amount=0.0;
    private String content ="";


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

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }
}
