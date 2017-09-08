package ke.co.suncha.simba.mobile.charts.pie;

/**
 * Created by maitha.manyala on 9/7/17.
 */
public class PEntry {
    private String label;
    private float value;

    public PEntry(String label, float value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
