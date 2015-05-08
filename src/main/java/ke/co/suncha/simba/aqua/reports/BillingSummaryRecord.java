package ke.co.suncha.simba.aqua.reports;

import ke.co.suncha.simba.aqua.models.BillItemType;

import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 5/7/15.
 */
public class BillingSummaryRecord {
    private Double billedOnActual;
    private Double billedOnEstimate;
    private Double creditAdjustments;
    private Double debitAdjustments;
    private Double totalCharges;
    private Double reconnectionFee;
    private Double atOwnersRequestFee;
    private Double changeOfAccountName;
    private  Double byPassFee;
    private  Double bouncedChequeFee;
    private Double surchargeIrrigation;
    private Double surchargeMissuse;
    private  Double meterServicing;


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
