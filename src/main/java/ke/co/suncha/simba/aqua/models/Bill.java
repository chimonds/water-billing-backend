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

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
 */
@Entity
@Table(name = "bills")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Bill extends SimbaBaseEntity implements Serializable {

	private static final long serialVersionUID = 8329325293910730469L;

	@Id
	@Column(name = "bill_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long billId;

	@NotNull
	@Column(name = "current_reading")
	private Integer currentReading = 0;

	@NotNull
	@Column(name = "previous_reading")
	private Integer previousReading = 0;

	@NotNull
	@Column(name = "units_billed")
	private Integer unitsBilled = 0;
	
	@NotNull
	@Column(name = "average_consumption")
	private Integer averageConsumption = 0;

	@NotNull
	@Column(name = "bill_code")
	private Integer billCode = 0;

	@NotNull
	@Column(name = "amount")
	private Double amount = (double) 0;

	@NotNull
	@Column(name = "meter_rent")
	private Double meterRent = (double) 0;
	
	@NotNull
	@Column(name = "total_billed")
	private Double totalBilled = (double) 0;
	
	@Transient
	private Boolean billed= true;
	
	

	@NotNull
	@Column(name = "transaction_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar transactionDate =Calendar.getInstance();

	@NotNull
	@Column(name = "consumption_type", length = 7)
	private String consumptionType;
	
	@Column (name ="content", length=4000)
	private String content="";

	// bills belong to an account
	@JsonIgnore
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id")
	private Account account;

	// Bills belong to a billing month
	@ManyToOne(fetch = FetchType.EAGER)
	@NotNull
	@JoinColumn(name = "billing_month_id")
	private BillingMonth billingMonth;
	
	
	// an account has a bills
	//@JsonIgnore
	@OneToMany(mappedBy = "bill", fetch = FetchType.EAGER )
	private List<BillItem> billItems;
	
	

	/**
	 * @return the billed
	 */
	public Boolean isBilled() {
		return billed;
	}

	/**
	 * @param billed the billed to set
	 */
	public void setBilled(Boolean billed) {
		this.billed = billed;
	}

	/**
	 * @return the unitsBilled
	 */
	public Integer getUnitsBilled() {
		return unitsBilled;
	}

	/**
	 * @param unitsBilled the unitsBilled to set
	 */
	public void setUnitsBilled(Integer unitsBilled) {
		this.unitsBilled = unitsBilled;
	}

	/**
	 * @return the totalBilled
	 */
	public Double getTotalBilled() {
		return totalBilled;
	}

	/**
	 * @param totalBilled the totalBilled to set
	 */
	public void setTotalBilled(Double totalBilled) {
		this.totalBilled = totalBilled;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the billItems
	 */
	public List<BillItem> getBillItems() {
		return billItems;
	}

	/**
	 * @param billItems the billItems to set
	 */
	public void setBillItems(List<BillItem> billItems) {
		this.billItems = billItems;
	}

	/**
	 * @return the billId
	 */
	public long getBillId() {
		return billId;
	}

	/**
	 * @param billId
	 *            the billId to set
	 */
	public void setBillId(long billId) {
		this.billId = billId;
	}

	/**
	 * @return the account
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * @param account
	 *            the account to set
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
	 * @param billingMonth
	 *            the billingMonth to set
	 */
	public void setBillingMonth(BillingMonth billingMonth) {
		this.billingMonth = billingMonth;
	}

	/**
	 * @return the currentReading
	 */
	public Integer getCurrentReading() {
		return currentReading;
	}

	/**
	 * @param currentReading
	 *            the currentReading to set
	 */
	public void setCurrentReading(Integer currentReading) {
		this.currentReading = currentReading;
	}

	/**
	 * @return the previousReading
	 */
	public Integer getPreviousReading() {
		return previousReading;
	}

	/**
	 * @param previousReading
	 *            the previousReading to set
	 */
	public void setPreviousReading(Integer previousReading) {
		this.previousReading = previousReading;
	}

	/**
	 * @return the averageConsumption
	 */
	public Integer getAverageConsumption() {
		return averageConsumption;
	}

	/**
	 * @param averageConsumption
	 *            the averageConsumption to set
	 */
	public void setAverageConsumption(Integer averageConsumption) {
		this.averageConsumption = averageConsumption;
	}

	/**
	 * @return the billCode
	 */
	public Integer getBillCode() {
		return billCode;
	}

	/**
	 * @param billCode
	 *            the billCode to set
	 */
	public void setBillCode(Integer billCode) {
		this.billCode = billCode;
	}

	/**
	 * @return the amount
	 */
	public Double getAmount() {
		return amount;
	}

	/**
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(Double amount) {
		this.amount = amount;
	}

	/**
	 * @return the meterRent
	 */
	public Double getMeterRent() {
		return meterRent;
	}

	/**
	 * @param meterRent
	 *            the meterRent to set
	 */
	public void setMeterRent(Double meterRent) {
		this.meterRent = meterRent;
	}


	/**
	 * @return the transactionDate
	 */
	public Calendar getTransactionDate() {
		return transactionDate;
	}

	/**
	 * @param transactionDate the transactionDate to set
	 */
	public void setTransactionDate(Calendar transactionDate) {
		this.transactionDate = transactionDate;
	}

	/**
	 * @return the consumptionType
	 */
	public String getConsumptionType() {
		return consumptionType;
	}

	/**
	 * @param consumptionType
	 *            the consumptionType to set
	 */
	public void setConsumptionType(String consumptionType) {
		this.consumptionType = consumptionType;
	}
}
