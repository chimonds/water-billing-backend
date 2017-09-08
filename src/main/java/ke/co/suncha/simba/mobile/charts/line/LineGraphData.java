package ke.co.suncha.simba.mobile.charts.line;

import ke.co.suncha.simba.mobile.charts.ChartType;
import ke.co.suncha.simba.mobile.charts.MobileChart;

import java.util.List;

/**
 * Created by maitha.manyala on 9/7/17.
 */
public class LineGraphData extends MobileChart {
    List<LineGraphDataset> lineGraphDatasetList;

    @Override
    public ChartType getChartType() {
        return ChartType.LINE_CHART;
    }

    public List<LineGraphDataset> getLineGraphDatasetList() {
        return lineGraphDatasetList;
    }

    public void setLineGraphDatasetList(List<LineGraphDataset> lineGraphDatasetList) {
        this.lineGraphDatasetList = lineGraphDatasetList;
    }
}
