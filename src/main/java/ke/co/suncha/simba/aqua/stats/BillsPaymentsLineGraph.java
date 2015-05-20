package ke.co.suncha.simba.aqua.stats;

import java.util.List;

/**
 * Created by manyala on 5/19/15.
 */
public class BillsPaymentsLineGraph {
    private XAxisMeta xAxisMeta;
    private List<GraphSeries> series;

    public List<GraphSeries> getSeries() {
        return series;
    }

    public void setSeries(List<GraphSeries> series) {
        this.series = series;
    }

    public XAxisMeta getxAxisMeta() {
        return xAxisMeta;
    }

    public void setxAxisMeta(XAxisMeta xAxisMeta) {
        this.xAxisMeta = xAxisMeta;
    }
}
