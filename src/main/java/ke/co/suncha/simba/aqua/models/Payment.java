/*
 * The MIT License
 *
 * Copyright 2015 Maitha Manyala.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ke.co.suncha.simba.aqua.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;
import ke.co.suncha.simba.aqua.account.Account;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Entity
@Table(name = "payments", indexes = {@Index(name = "i_payments_trans_date", columnList = "transaction_date", unique = false), @Index(name = "i_payments_receipt_no", columnList = "receipt_no", unique = false)})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Payment extends SimbaBaseEntity implements Serializable {

    private static final long serialVersionUID = -656163045643445357L;
    @Id
    @Column(name = "payment_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long paymentid;

    @Column(name = "amount", nullable = false)
    private Double amount = (double) 0;

    @Column(name = "transaction_date")
    @Temporal(TemporalType.TIMESTAMP)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime transactionDate = new DateTime();

    @NotNull
    @Column(name = "receipt_no", length = 15)
    private String receiptNo;

    @Column(name = "is_multi_part")
    private Boolean isMultiPart = false;

    @Column(name = "ref_no")
    private String refNo;

    @NotNull
    @Column(name = "is_void", length = 1)
    private Integer isVoid = 0;

    @Column(name = "comments", length = 1000)
    private String notes = "";

    @Transient
    private String accNo;

    // payments belong to an account
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    // payments belong to a billing month
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "billing_month_id")
    private BillingMonth billingMonth;

    // a payment has a payment type
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_type_id")
    private PaymentType paymentType;

    // a payment has a payment source
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_source_id")
    private PaymentSource paymentSource;

    public String getAccNo() {
        if (this.getAccount() != null) {
            this.accNo = this.getAccount().getAccNo();
        }
        return accNo;
    }


    /**
     * @return the paymentSource
     */
    public PaymentSource getPaymentSource() {
        return paymentSource;
    }

    /**
     * @param paymentSource the paymentSource to set
     */
    public void setPaymentSource(PaymentSource paymentSource) {
        this.paymentSource = paymentSource;
    }

    /**
     * @return the paymentid
     */
    public Long getPaymentid() {
        return paymentid;
    }

    /**
     * @param paymentid the paymentid to set
     */
    public void setPaymentid(Long paymentid) {
        this.paymentid = paymentid;
    }

    /**
     * @return the amount
     */
    public Double getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(Double amount) {
        this.amount = amount;
    }


    public DateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(DateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    /**
     * @return the receiptNo
     */
    public String getReceiptNo() {
        return receiptNo;
    }

    /**
     * @param receiptNo the receiptNo to set
     */
    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
    }

    /**
     * @return the isVoid
     */
    public Integer getIsVoid() {
        return isVoid;
    }

    /**
     * @param isVoid the isVoid to set
     */
    public void setIsVoid(Integer isVoid) {
        this.isVoid = isVoid;
    }

    /**
     * @return the comments
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the comments to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the account
     */
    public Account getAccount() {
        return account;
    }

    /**
     * @param account the account to set
     */
    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * @return the billingMonth
     */
    public BillingMonth getBillingMonth() {
        return billingMonth;
    }

    /**
     * @param billingMonth the billingMonth to set
     */
    public void setBillingMonth(BillingMonth billingMonth) {
        this.billingMonth = billingMonth;
    }

    /**
     * @return the paymentType
     */
    public PaymentType getPaymentType() {
        return paymentType;
    }

    /**
     * @param paymentType the paymentType to set
     */
    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public Boolean getIsMultiPart() {
        return isMultiPart;
    }

    public void setIsMultiPart(Boolean isMultiPart) {
        this.isMultiPart = isMultiPart;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentid=" + paymentid +
                ", amount=" + amount +
                ", transactionDate=" + transactionDate +
                ", receiptNo='" + receiptNo + '\'' +
                ", isVoid=" + isVoid +
                ", notes='" + notes + '\'' +
                '}';
    }
}
