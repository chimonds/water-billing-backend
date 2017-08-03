package ke.co.suncha.simba.aqua.reports.waris;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maitha.manyala on 8/2/17.
 */
public class WarisDetailed {
    String categoryName;
    Double meteredVolume;
    Double unMeteredVolume;
    Double meteredWaterSale;
    Double unMeteredWaterSale;
    Double meterRent;
    List<OtherService> OtherServices= new ArrayList<>();
    List<OtherService> receipts= new ArrayList<>();

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Double getMeteredVolume() {
        return meteredVolume;
    }

    public void setMeteredVolume(Double meteredVolume) {
        this.meteredVolume = meteredVolume;
    }

    public Double getUnMeteredVolume() {
        return unMeteredVolume;
    }

    public void setUnMeteredVolume(Double unMeteredVolume) {
        this.unMeteredVolume = unMeteredVolume;
    }

    public Double getMeteredWaterSale() {
        return meteredWaterSale;
    }

    public void setMeteredWaterSale(Double meteredWaterSale) {
        this.meteredWaterSale = meteredWaterSale;
    }

    public Double getUnMeteredWaterSale() {
        return unMeteredWaterSale;
    }

    public void setUnMeteredWaterSale(Double unMeteredWaterSale) {
        this.unMeteredWaterSale = unMeteredWaterSale;
    }

    public Double getMeterRent() {
        return meterRent;
    }

    public void setMeterRent(Double meterRent) {
        this.meterRent = meterRent;
    }

    public List<OtherService> getOtherServices() {
        return OtherServices;
    }

    public void setOtherServices(List<OtherService> otherServices) {
        OtherServices = otherServices;
    }

    public List<OtherService> getReceipts() {
        return receipts;
    }

    public void setReceipts(List<OtherService> receipts) {
        this.receipts = receipts;
    }
}
