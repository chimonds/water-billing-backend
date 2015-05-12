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
