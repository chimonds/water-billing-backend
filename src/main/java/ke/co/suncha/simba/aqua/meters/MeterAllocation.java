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

import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;
import ke.co.suncha.simba.aqua.account.Account;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
 */
@Entity
@Table(name = "meter_allocations")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MeterAllocation extends SimbaBaseEntity implements Serializable {
	private static final long serialVersionUID = -3226144448168997314L;

	@Id
	@Column(name = "meter_allocation_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long meterAllocationId;

	@Column(name = "notes", length = 1000)
	private String notes;
	
	@Column(name = "alocation_type", length = 15)
	private String allocationType;
	

	@Column(name = "reading")
	private Double reading = 0.0;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "meter_id")
	private Meter meter;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id")
	private Account account;

	
	
	/**
	 * @return the allocationType
	 */
	public String getAllocationType() {
		return allocationType;
	}

	/**
	 * @param allocationType the allocationType to set
	 */
	public void setAllocationType(String allocationType) {
		this.allocationType = allocationType;
	}

	/**
	 * @return the meterAllocationId
	 */
	public long getMeterAllocationId() {
		return meterAllocationId;
	}

	/**
	 * @param meterAllocationId
	 *            the meterAllocationId to set
	 */
	public void setMeterAllocationId(long meterAllocationId) {
		this.meterAllocationId = meterAllocationId;
	}

	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * @param notes
	 *            the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * @return the reading
	 */
	public Double getReading() {
		return reading;
	}

	/**
	 * @param reading
	 *            the reading to set
	 */
	public void setReading(Double reading) {
		this.reading = reading;
	}

	/**
	 * @return the meter
	 */
	public Meter getMeter() {
		return meter;
	}

	/**
	 * @param meter
	 *            the meter to set
	 */
	public void setMeter(Meter meter) {
		this.meter = meter;
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

}
