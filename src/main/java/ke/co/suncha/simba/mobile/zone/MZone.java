package ke.co.suncha.simba.mobile.zone;

/**
 * Created by maitha.manyala on 8/15/17.
 */
public class MZone {
    private Long zoneId;
    private String name;
    private Integer takeReadings = 0;

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTakeReadings() {
        return takeReadings;
    }

    public void setTakeReadings(Integer takeReadings) {
        this.takeReadings = takeReadings;
    }
}
