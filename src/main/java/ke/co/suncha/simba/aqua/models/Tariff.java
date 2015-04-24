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
import java.util.List;

import javax.persistence.*;
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
@Table(name = "tariffs")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Tariff extends SimbaBaseEntity implements Serializable {

	private static final long serialVersionUID = 6551903786424076503L;
	@Id
	@Column(name = "tariff_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long tariffId;

	@NotNull
	@Column(name = "name", unique = true)
	private String name;

	@Column(name = "description")
	private String description;

	// a tariff has tariff matrixes
	@JsonIgnore
	@OneToMany(mappedBy = "tariff", fetch = FetchType.LAZY )
	private List<TariffMatrix> tariffMatrixes;



	/**
	 * @return the tariffId
	 */
	public long getTariffId() {
		return tariffId;
	}

	/**
	 * @param tariffId the tariffId to set
	 */
	public void setTariffId(long tariffId) {
		this.tariffId = tariffId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
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
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public List<TariffMatrix> getTariffMatrixes() {
		return tariffMatrixes;
	}

	public void setTariffMatrixes(List<TariffMatrix> tariffMatrixes) {
		this.tariffMatrixes = tariffMatrixes;
	}
}
