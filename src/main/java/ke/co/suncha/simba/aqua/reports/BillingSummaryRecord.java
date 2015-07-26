package ke.co.suncha.simba.aqua.reports;

import ke.co.suncha.simba.aqua.models.BillItemType;

import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 5/7/15.
 */
public class BillingSummaryRecord {
    private Double billedOnActual = 0.0;
    private Double billedOnEstimate = 0.0;
    private Double creditAdjustments = 0.0;
    private Double debitAdjustments = 0.0;
    private Double totalCharges = 0.0;
    private Double reconnectionFee = 0.0;
    private Double atOwnersRequestFee = 0.0;
    private Double changeOfAccountName = 0.0;
    private Double byPassFee = 0.0;
    private Double bouncedChequeFee = 0.0;
    private Double surchargeIrrigation = 0.0;
    private Double surchargeMissuse = 0.0;
    private Double meterServicing = 0.0;

    //additional for waris report
    private Integer activeAccounts = 0;
    private Integer inactiveAccounts = 0;

    private Integer unitsActualConsumption = 0;
    private Integer unitsEstimatedConsumption = 0;

    private Double balancesActiveAccounts = 0.0;
    private Double balancesInactiveAccounts = 0.0;

    private Double totalPayments=0.0;

    private Integer activeMeteredAccounts=0;
    private Integer activeUnMeteredAccounts=0;

    private Integer meteredBilledActual=0;
    private Integer meteredBilledAverage=0;

    public Integer getMeteredBilledActual() {
        return meteredBilledActual;
    }

    public void setMeteredBilledActual(Integer meteredBilledActual) {
        this.meteredBilledActual = meteredBilledActual;
    }

    public Integer getMeteredBilledAverage() {
        return meteredBilledAverage;
    }

    public void setMeteredBilledAverage(Integer meteredBilledAverage) {
        this.meteredBilledAverage = meteredBilledAverage;
    }

    public Integer getActiveMeteredAccounts() {
        return activeMeteredAccounts;
    }

    public void setActiveMeteredAccounts(Integer activeMeteredAccounts) {
        this.activeMeteredAccounts = activeMeteredAccounts;
    }

    public Integer getActiveUnMeteredAccounts() {
        return activeUnMeteredAccounts;
    }

    public void setActiveUnMeteredAccounts(Integer activeUnMeteredAccounts) {
        this.activeUnMeteredAccounts = activeUnMeteredAccounts;
    }

    public Double getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(Double totalPayments) {
        this.totalPayments = totalPayments;
    }

    public Double getBalancesActiveAccounts() {
        return balancesActiveAccounts;
    }

    public void setBalancesActiveAccounts(Double balancesActiveAccounts) {
        this.balancesActiveAccounts = balancesActiveAccounts;
    }

    public Double getBalancesInactiveAccounts() {
        return balancesInactiveAccounts;
    }

    public void setBalancesInactiveAccounts(Double balancesInactiveAccounts) {
        this.balancesInactiveAccounts = balancesInactiveAccounts;
    }

    public Integer getActiveAccounts() {
        return activeAccounts;
    }

    public void setActiveAccounts(Integer activeAccounts) {
        this.activeAccounts = activeAccounts;
    }

    public Integer getInactiveAccounts() {
        return inactiveAccounts;
    }

    public void setInactiveAccounts(Integer inactiveAccounts) {
        this.inactiveAccounts = inactiveAccounts;
    }

    public Integer getUnitsActualConsumption() {
        return unitsActualConsumption;
    }

    public void setUnitsActualConsumption(Integer unitsActualConsumption) {
        this.unitsActualConsumption = unitsActualConsumption;
    }

    public Integer getUnitsEstimatedConsumption() {
        return unitsEstimatedConsumption;
    }

    public void setUnitsEstimatedConsumption(Integer unitsEstimatedConsumption) {
        this.unitsEstimatedConsumption = unitsEstimatedConsumption;
    }

    public Double getBilledOnActual() {
        return billedOnActual;
    }

    public void setBilledOnActual(Double billedOnActual) {
        this.billedOnActual = billedOnActual;
    }

    public Double getBilledOnEstimate() {
        return billedOnEstimate;
    }

    public void setBilledOnEstimate(Double billedOnEstimate) {
        this.billedOnEstimate = billedOnEstimate;
    }

    public Double getCreditAdjustments() {
        return creditAdjustments;
    }

    public void setCreditAdjustments(Double creditAdjustments) {
        this.creditAdjustments = creditAdjustments;
    }

    public Double getDebitAdjustments() {
        return debitAdjustments;
    }

    public void setDebitAdjustments(Double debitAdjustments) {
        this.debitAdjustments = debitAdjustments;
    }

    public Double getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(Double totalCharges) {
        this.totalCharges = totalCharges;
    }

    public Double getReconnectionFee() {
        return reconnectionFee;
    }

    public void setReconnectionFee(Double reconnectionFee) {
        this.reconnectionFee = reconnectionFee;
    }

    public Double getAtOwnersRequestFee() {
        return atOwnersRequestFee;
    }

    public void setAtOwnersRequestFee(Double atOwnersRequestFee) {
        this.atOwnersRequestFee = atOwnersRequestFee;
    }

    public Double getChangeOfAccountName() {
        return changeOfAccountName;
    }

    public void setChangeOfAccountName(Double changeOfAccountName) {
        this.changeOfAccountName = changeOfAccountName;
    }

    public Double getByPassFee() {
        return byPassFee;
    }

    public void setByPassFee(Double byPassFee) {
        this.byPassFee = byPassFee;
    }

    public Double getBouncedChequeFee() {
        return bouncedChequeFee;
    }

    public void setBouncedChequeFee(Double bouncedChequeFee) {
        this.bouncedChequeFee = bouncedChequeFee;
    }

    public Double getSurchargeIrrigation() {
        return surchargeIrrigation;
    }

    public void setSurchargeIrrigation(Double surchargeIrrigation) {
        this.surchargeIrrigation = surchargeIrrigation;
    }

    public Double getSurchargeMissuse() {
        return surchargeMissuse;
    }

    public void setSurchargeMissuse(Double surchargeMissuse) {
        this.surchargeMissuse = surchargeMissuse;
    }

    public Double getMeterServicing() {
        return meterServicing;
    }

    public void setMeterServicing(Double meterServicing) {
        this.meterServicing = meterServicing;
    }
}
