package ke.co.suncha.simba.mobile.charts.line;

import java.util.List;

/**
 * Created by maitha.manyala on 9/7/17.
 */

public class LineGraphDataset {
    private String name;
    private String color;
    private String circleColor;
    private List<LineEntry> lineEntries;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCircleColor() {
        return circleColor;
    }

    public void setCircleColor(String circleColor) {
        this.circleColor = circleColor;
    }

    public List<LineEntry> getLineEntries() {
        return lineEntries;
    }

    public void setLineEntries(List<LineEntry> lineEntries) {
        this.lineEntries = lineEntries;
    }
}
