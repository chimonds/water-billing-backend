package ke.co.suncha.simba.mobile.charts.card;

import ke.co.suncha.simba.mobile.charts.ChartType;
import ke.co.suncha.simba.mobile.charts.MobileChart;

/**
 * Created by maitha.manyala on 9/7/17.
 */
public class CardStatData extends MobileChart {
    private String label;
    private String value;

    @Override
    public ChartType getChartType() {
        return ChartType.CARD;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}