package ke.co.suncha.simba.aqua.stats;

/**
 * Created by manyala on 5/19/15.
 */
public class StatsResponse {
    private TopView topView;
    private BillsPaymentsLineGraph billsPaymentsLineGraph;

    public StatsResponse() {
        this.topView = new TopView();
        this.billsPaymentsLineGraph = new BillsPaymentsLineGraph();
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
