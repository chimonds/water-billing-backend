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

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Entity
@Table(name = "meters")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Meter extends SimbaBaseEntity implements Serializable {

    private static final long serialVersionUID = 7476663980584980131L;

    @Id
    @Column(name = "meter_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long meterId;

    @NotNull
    @Column(name = "meter_no", unique = true, length = 20)
    private String meterNo;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "notes", length = 1000)
    private String notes;

    @NotNull
    @Column(name = "initial_reading")
    private Integer initialReading = 0;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "meter_owner_id")
    private MeterOwner meterOwner;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "meter_size_id")
    private MeterSize meterSize;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "meter")
    private Account account;

    @Transient
    private Boolean assigned = false;

    @Column(name = "is_new")
    private Boolean isNew = Boolean.FALSE;

    @Transient
    private String accountId = "Not Available";

    @Column(name = "can_be_allocated")
    private Boolean canBeAllocated = false;

    @JsonIgnore
    @OneToMany(mappedBy = "meter")
    private List<MeterAllocation> meterAllocations;


    /**
     * @return the accountId
     */
    public String getAccountId() {
        if (this.account != null) {
            this.accountId = account.getAccNo().toString();
        }
        return accountId;
    }

    /**
     * @param accountId the accountId to set
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * @return the assigned
     */
    public Boolean getAssigned() {
        if (this.account != null) {
            this.assigned = true;
        }
        return assigned;
    }

    /**
     * @param assigned the assigned to set
     */
    public void setAssigned(Boolean assigned) {
        this.assigned = assigned;
    }

    /**
     * @return the meterAllocations
     */
    public List<MeterAllocation> getMeterAllocations() {
        return meterAllocations;
    }

    /**
     * @param meterAllocations the meterAllocations to set
     */
    public void setMeterAllocations(List<MeterAllocation> meterAllocations) {
        this.meterAllocations = meterAllocations;
    }

    /**
     * @return the initialReading
     */
    public Integer getInitialReading() {
        return initialReading;
    }

    /**
     * @param initialReading the initialReading to set
     */
    public void setInitialReading(Integer initialReading) {
        this.initialReading = initialReading;
    }

    /**
     * @return the meterId
     */
    public long getMeterId() {
        return meterId;
    }

    /**
     * @param meterId the meterId to set
     */
    public void setMeterId(long meterId) {
        this.meterId = meterId;
    }

    /**
     * @return the meterNo
     */
    public String getMeterNo() {
        return meterNo;
    }

    /**
     * @param meterNo the meterNo to set
     */
    public void setMeterNo(String meterNo) {
        this.meterNo = meterNo;
    }

    /**
     * @return the active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the meterOwner
     */
    public MeterOwner getMeterOwner() {
        return meterOwner;
    }

    /**
     * @param meterOwner the meterOwner to set
     */
    public void setMeterOwner(MeterOwner meterOwner) {
        this.meterOwner = meterOwner;
    }

    /**
     * @return the meterSize
     */
    public MeterSize getMeterSize() {
        return meterSize;
    }

    /**
     * @param meterSize the meterSize to set
     */
    public void setMeterSize(MeterSize meterSize) {
        this.meterSize = meterSize;
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

    public Boolean getCanBeAllocated() {
        return canBeAllocated;
    }

    public void setCanBeAllocated(Boolean canBeAllocated) {
        this.canBeAllocated = canBeAllocated;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }

    @Override
    public String toString() {
        return "Meter{" +
                "meterId=" + meterId +
                ", meterNo='" + meterNo + '\'' +
                ", active=" + active +
                ", notes='" + notes + '\'' +
                ", initialReading=" + initialReading +
                ", meterOwner=" + meterOwner +
                ", meterSize=" + meterSize +
                ", assigned=" + assigned +
                '}';
    }
}
