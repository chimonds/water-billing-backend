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
@Table(name = "billing_months")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BillingMonth extends SimbaBaseEntity implements Serializable {

    private static final long serialVersionUID = -5067080841809492162L;

    @Id
    @Column(name = "billing_month_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long billingMonthId;

    @Temporal(TemporalType.DATE)
    @Column(name = "billing_month", unique = true)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime month;

    @NotNull
    @Column(name = "code", unique = true)
    private Integer code;

    @Column(name = "is_current")
    private Integer current = 0;

    @Column(name = "is_enabled")
    private Integer isEnabled = 0;

    @Column(name = "is_meter_reading")
    private Boolean meterReading = Boolean.FALSE;

    @Transient
    private Boolean active;

    @Transient
    private String open;

    public String getOpen() {
        if (this.isActive()) {
            this.open = "Open";
        } else {
            this.open = "Closed";
        }
        return open;
    }


    /**
     * @param active the isActive to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * @return the billingMonthId
     */
    public long getBillingMonthId() {
        return billingMonthId;
    }

    /**
     * @param billingMonthId the billingMonthId to set
     */
    public void setBillingMonthId(long billingMonthId) {
        this.billingMonthId = billingMonthId;
    }


    public DateTime getMonth() {
        return month;
    }

    public void setMonth(DateTime month) {
        this.month = month;
    }

    /**
     * @return the code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * @return the isCurrent
     */
    public Integer getCurrent() {
        return current;
    }

    /**
     * @param current the isCurrent to set
     */
    public void setCurrent(Integer current) {
        this.current = current;
    }

    /**
     * @return the isEnabled
     */
    public Integer getIsEnabled() {
        return isEnabled;
    }

    /**
     * @param isEnabled the isEnabled to set
     */
    public void setIsEnabled(Integer isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * @return the isActive
     */
    public Boolean isActive() {
        if (this.current == 0) {
            this.active = false;
        } else if (this.current == 1) {
            this.active = true;
        }
        return active;
    }

    public Boolean getMeterReading() {
        return meterReading;
    }

    public void setMeterReading(Boolean meterReading) {
        this.meterReading = meterReading;
    }

    @Override
    public String toString() {
        return "BillingMonth{" +
                "billingMonthId=" + billingMonthId +
                ", month=" + month +
                ", code=" + code +
                ", current=" + current +
                ", isEnabled=" + isEnabled +
                ", active=" + active +
                '}';
    }
}
