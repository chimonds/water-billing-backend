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
@Table(name = "payment_sources")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentSource extends SimbaBaseEntity implements Serializable {

    private static final long serialVersionUID = 4536954085077053415L;

    @Id
    @Column(name = "payment_source_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long paymentSourceId;

    @NotNull
    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "acknowledge_sms")
    private Boolean acknowledgeSMS = Boolean.TRUE;

    /**
     * @return the paymentSourceId
     */
    public long getPaymentSourceId() {
        return paymentSourceId;
    }

    /**
     * @param paymentSourceId the paymentSourceId to set
     */
    public void setPaymentSourceId(long paymentSourceId) {
        this.paymentSourceId = paymentSourceId;
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

    public Boolean getAcknowledgeSMS() {
        return acknowledgeSMS;
    }

    public void setAcknowledgeSMS(Boolean acknowledgeSMS) {
        this.acknowledgeSMS = acknowledgeSMS;
    }
}
