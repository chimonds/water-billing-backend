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
package ke.co.suncha.simba.admin.helpers;

import java.util.Calendar;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
 */
@MappedSuperclass
public abstract class SimbaBaseEntity {

	@NotNull
	@Column(name = "transaction_id", length = 100)
	@JsonIgnore
	private String transationId = UUID.randomUUID().toString();

	@Column(name = "created_on")
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar createdOn = Calendar.getInstance();

	@NotNull
	@Column(name = "approval_level", length = 0)
	private Integer approvalLevel = 0;

	/**
	 * @return the approvalLevel
	 */
	public Integer getApprovalLevel() {
		return approvalLevel;
	}

	/**
	 * @param approvalLevel
	 *            the approvalLevel to set
	 */
	public void setApprovalLevel(Integer approvalLevel) {
		this.approvalLevel = approvalLevel;
	}



	/**
	 * @return the createdOn
	 */
	public Calendar getCreatedOn() {
		return createdOn;
	}

	/**
	 * @param createdOn the createdOn to set
	 */
	public void setCreatedOn(Calendar createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * @return the transationId
	 */
	public String getTransationId() {
		if (this.transationId == null) {
			this.transationId = UUID.randomUUID().toString();
		}
		return transationId;
	}

	/**
	 * @param transationId
	 *            the transationId to set
	 */
	public void setTransationId(String transationId) {
		this.transationId = transationId;
	}
}
