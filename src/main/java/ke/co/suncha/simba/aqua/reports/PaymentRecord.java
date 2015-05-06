package ke.co.suncha.simba.aqua.reports;

import java.util.Calendar;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 5/6/15.
 */
public class PaymentRecord extends BaseRecord {
    private Calendar transactionDate;
    private String receiptNo;
    private String paymentType;



    public String getReceiptNo() {
        return receiptNo;
    }

    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
    }

    public Calendar getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Calendar transactionDate) {
        this.transactionDate = transactionDate;
    }


    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
}
