package ke.co.suncha.simba.aqua.stats;

import ke.co.suncha.simba.aqua.services.ZonesBarGraph;

/**
 * Created by manyala on 5/19/15.
 */
public class StatsResponse {
    private TopView topView;
    private BillsPaymentsLineGraph billsPaymentsLineGraph;
    private ZonesBarGraph zonesBarGraph;


    public StatsResponse() {
        this.topView = new TopView();
        this.billsPaymentsLineGraph = new BillsPaymentsLineGraph();
        this.zonesBarGraph= new ZonesBarGraph();
    }

    public ZonesBarGraph getZonesBarGraph() {
        return zonesBarGraph;
    }

    public void setZonesBarGraph(ZonesBarGraph zonesBarGraph) {
        this.zonesBarGraph = zonesBarGraph;
    }

    public TopView getTopView() {
        return topView;
    }

    public void setTopView(TopView topView) {
        this.topView = topView;
    }

    public BillsPaymentsLineGraph getBillsPaymentsLineGraph() {
        return billsPaymentsLineGraph;
    }

    public void setBillsPaymentsLineGraph(BillsPaymentsLineGraph billsPaymentsLineGraph) {
        this.billsPaymentsLineGraph = billsPaymentsLineGraph;
    }
}
