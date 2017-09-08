package ke.co.suncha.simba.mobile.charts;

/**
 * Created by maitha.manyala on 9/7/17.
 */
public class MobileChart implements Comparable<MobileChart> {
    private int sequence;
    private ChartType chartType = ChartType.UNKNOWN;

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }

    @Override
    public int compareTo(MobileChart chart) {
        if (sequence == chart.getSequence())
            return 1;
        else if (sequence > chart.getSequence())
            return 0;
        else
            return -1;
    }
}
