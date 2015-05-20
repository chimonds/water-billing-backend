package ke.co.suncha.simba.aqua.stats;

import java.util.List;

/**
 * Created by manyala on 5/19/15.
 */
public class GraphSeries {
    private String name;
    private List<Double> data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Double> getData() {
        return data;
    }

    public void setData(List<Double> data) {
        this.data = data;
    }
}
