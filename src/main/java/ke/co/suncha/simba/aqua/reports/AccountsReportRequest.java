package ke.co.suncha.simba.aqua.reports;

import java.io.Serializable;

/**
 * Created by maitha.manyala on 7/29/16.
 */
public class AccountsReportRequest implements Serializable {
    private Integer onStatus;
    private Boolean cutOff;
    private Long schemeId;
    private Long zoneId;

    public Integer getOnStatus() {
        return onStatus;
    }

    public void setOnStatus(Integer onStatus) {
        this.onStatus = onStatus;
    }

    public Boolean getIsCutOff() {
        return cutOff;
    }

    public void setCutOff(Boolean cutOff) {
        this.cutOff = cutOff;
    }

    public Long getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(Long schemeId) {
        this.schemeId = schemeId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }
}
