package ke.co.suncha.simba.mobile.charts.pie;

import ke.co.suncha.simba.mobile.charts.ChartType;
import ke.co.suncha.simba.mobile.charts.MobileChart;

import java.util.List;

/**
 * Created by maitha.manyala on 9/7/17.
 */

public class PieChartData extends MobileChart {
    private String name;
    private String centerText;
    private String valueTextColour;
    private List<PEntry> entries;
    private List<String> colours;

    @Override
    public ChartType getChartType() {
        return ChartType.PIE_CHART;
    }

    public String getValueTextColour() {
        return valueTextColour;
    }

    public void setValueTextColour(String valueTextColour) {
        this.valueTextColour = valueTextColour;
    }

    public String getCenterText() {
        return centerText;
    }

    public void setCenterText(String centerText) {
        this.centerText = centerText;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<PEntry> entries) {
        this.entries = entries;
    }

    public List<String> getColours() {
        return colours;
    }

    public void setColours(List<String> colours) {
        this.colours = colours;
    }
}

