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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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
@Table(name = "bill_items")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BillItem extends SimbaBaseEntity implements Serializable {
	private static final long serialVersionUID = 7537417103746150231L;

	@Id
	@Column(name = "bill_item_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long billItemId;

	@NotNull
	@Column(name = "amount")
	private Double amount = (double) 0;

	// a bill item has a bill iitem type
	@ManyToOne(fetch = FetchType.EAGER)
	@NotNull
	@JoinColumn(name = "bill_item_type_id")
	private BillItemType billItemType;

	@JsonIgnore
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bill_id")
	private Bill bill;

	/**
	 * @return the billItemId
	 */
	public long getBillItemId() {
		return billItemId;
	}

	/**
	 * @param billItemId
	 *            the billItemId to set
	 */
	public void setBillItemId(long billItemId) {
		this.billItemId = billItemId;
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
	 * @return the billItemType
	 */
	public BillItemType getBillItemType() {
		return billItemType;
	}

	/**
	 * @param billItemType the billItemType to set
	 */
	public void setBillItemType(BillItemType billItemType) {
		this.billItemType = billItemType;
	}

	/**
	 * @return the bill
	 */
	public Bill getBill() {
		return bill;
	}

	/**
	 * @param bill
	 *            the bill to set
	 */
	public void setBill(Bill bill) {
		this.bill = bill;
	}

}
