package ke.co.suncha.simba.aqua.reports;

import org.joda.time.DateTime;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 5/6/15.
 */
public class PaymentRecord extends BaseRecord {
    private DateTime transactionDate;
    private String receiptNo;
    private String paymentType;


    public String getReceiptNo() {
        return receiptNo;
    }

    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
    }

    public DateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(DateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
}
