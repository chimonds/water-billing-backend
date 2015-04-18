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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
 */
@Entity
@Table(name = "payment_types")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentType extends SimbaBaseEntity implements Serializable {

	private static final long serialVersionUID = 4737563981322228633L;

	@Id
	@Column(name = "payment_type_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long paymentTypeId;

	@NotNull
	@Column(name = "name", unique = true)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "is_primary")
	private Boolean primary = false;

	@NotNull
	@Column(name = "is_unique")
	private Boolean unique = false;

	@NotNull
	@Column(name = "has_comments")
	private Boolean comments = false;

	@NotNull
	@Column(name = "is_negative")
	private Boolean negative = false;

	/**
	 * @return the negative
	 */
	public Boolean isNegative() {
		return negative;
	}

	/**
	 * @param negative
	 *            the negative to set
	 */
	public void setNegative(Boolean negative) {
		this.negative = negative;
	}

	/**
	 * @return the comment
	 */
	public Boolean hasComments() {
		return comments;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(Boolean comment) {
		this.comments = comment;
	}

	/**
	 * @return the isUnique
	 */
	public Boolean isUnique() {
		return unique;
	}

	/**
	 * @param isUnique
	 *            the isUnique to set
	 */
	public void setUnique(Boolean unique) {
		this.unique = unique;
	}

	/**
	 * @return the isPrimary
	 */
	public Boolean getPrimary() {
		return primary;
	}

	/**
	 * @param isPrimary
	 *            the isPrimary to set
	 */
	public void setIsPrimary(Boolean isPrimary) {
		this.primary = isPrimary;
	}

	/**
	 * @return the paymentTypeId
	 */
	public long getPaymentTypeId() {
		return paymentTypeId;
	}

	/**
	 * @param paymentTypeId
	 *            the paymentTypeId to set
	 */
	public void setPaymentTypeId(long paymentTypeId) {
		this.paymentTypeId = paymentTypeId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
