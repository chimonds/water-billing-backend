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
@Table(name = "accounts")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Account extends SimbaBaseEntity implements Serializable {
    private static final long serialVersionUID = 432730479655553234L;

    @Id
    @Column(name = "account_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long accountId;

    @NotNull
    @Column(name = "balance_bf")
    private Double balanceBroughtForward = (double) 0;

    @Column(name = "update_balance")
    private Boolean updateBalance = true;

    @NotNull
    @Column(name = "outstandingBalance")
    private Double outstandingBalance = (double) 0;

    @NotNull
    @Column(name = "acc_no", unique = true, length = 20)
    private String accNo;

    @Transient
    private String accName;

    @Transient
    private String accountStatus;

    @Column(name = "average_consumption")
    private Integer averageConsumption = 0;

    @Column(name = "cut_off")
    private Boolean active = true;

    @Transient
    private Boolean metered = false;

    @NotNull
    @Column(name = "water_sale_balance")
    private Double waterSaleBalance = 0d;

    @NotNull
    @Column(name = "meter_rent_balance")
    private Double meterRentBalance = 0d;

    @NotNull
    @Column(name = "penalties_balance")
    private Double penaltiesBalance = 0d;

    // account belongs to a consumer
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id")
    private Consumer consumer;


    // an account has a location
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    private Location location;

    // an account has a zone
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    // an account has a tariff
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tariff_id")
    private Tariff tariff;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_category_id")
    private AccountCategory accountCategory;

    // an account has a bills
    @JsonIgnore
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bill> bills;

    // an account has a meter
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "meter_id")
    private Meter meter;

    // an account has a payments
    @JsonIgnore
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    @JsonIgnore
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccountStatusHistory> accountStatusHistoryList;

    public List<AccountStatusHistory> getAccountStatusHistoryList() {
        return accountStatusHistoryList;
    }

    public void setAccountStatusHistoryList(List<AccountStatusHistory> accountStatusHistoryList) {
        this.accountStatusHistoryList = accountStatusHistoryList;
    }

    public String getAccountStatus() {
        if (this.active != null) {
            if (this.isActive()) {
                this.accountStatus = "Active";
            } else {
                this.accountStatus = "Inactive";
            }
        }
        return accountStatus;
    }


    /**
     * @return the accName
     */

    public String getAccName() {
        if (this.getConsumer() != null) {

            String fullName = "";
            if (this.getConsumer().getFirstName() != null) {
                fullName = this.getConsumer().getFirstName();
            }

            if (this.getConsumer().getMiddleName() != null) {
                fullName = fullName + " " + this.getConsumer().getMiddleName();
            }

            if (this.getConsumer().getLastName() != null) {
                fullName = fullName + " " + this.getConsumer().getLastName();
            }
            this.accName = fullName;
        } else {
            this.accName = "Not Assigned";
        }
        return accName;
    }

    /**
     * @return the metered
     */
    public Boolean isMetered() {
        if (this.getMeter() != null) {
            this.metered = true;
        }
        return metered;
    }

    /**
     * @return the meter
     */
    public Meter getMeter() {
        return meter;
    }

    /**
     * @param meter the meter to set
     */
    public void setMeter(Meter meter) {
        this.meter = meter;
    }

    /**
     * @return the outstandingBalance
     */
    public Double getOutstandingBalance() {
        return outstandingBalance;
    }

    /**
     * @param outstandingBalance the outstandingBalance to set
     */
    public void setOutstandingBalance(Double outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    /**
     * @return the averageConsumption
     */
    public Integer getAverageConsumption() {
        return averageConsumption;
    }

    /**
     * @param averageConsumption the averageConsumption to set
     */
    public void setAverageConsumption(Integer averageConsumption) {
        this.averageConsumption = averageConsumption;
    }

    /**
     * @return the payments
     */
    public List<Payment> getPayments() {
        return payments;
    }

    /**
     * @param payments the payments to set
     */
    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    /**
     * @return the bills
     */
    public List<Bill> getBills() {
        return bills;
    }

    /**
     * @param bills the bills to set
     */
    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }


    /**
     * @return the active
     */
    public Boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * @return the tariff
     */
    public Tariff getTariff() {
        return tariff;
    }

    /**
     * @param tariff the tariff to set
     */
    public void setTariff(Tariff tariff) {
        this.tariff = tariff;
    }

    /**
     * @return the zone
     */
    public Zone getZone() {
        return zone;
    }

    /**
     * @param zone the zone to set
     */
    public void setZone(Zone zone) {
        this.zone = zone;
    }

    /**
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * @return the accountId
     */
    public Long getAccountId() {
        return accountId;
    }

    /**
     * @param accountId the accountId to set
     */
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    /**
     * @return the balanceBroughtForward
     */
    public Double getBalanceBroughtForward() {
        return balanceBroughtForward;
    }

    /**
     * @param balanceBroughtForward the balanceBroughtForward to set
     */
    public void setBalanceBroughtForward(Double balanceBroughtForward) {
        this.balanceBroughtForward = balanceBroughtForward;
    }

    /**
     * @return the accNo
     */
    public String getAccNo() {
        return accNo;
    }

    /**
     * @param accNo the accNo to set
     */
    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    /**
     * @return the consumer
     */
    public Consumer getConsumer() {
        return consumer;
    }

    /**
     * @param consumer the consumer to set
     */
    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public AccountCategory getAccountCategory() {
        return accountCategory;
    }

    public void setAccountCategory(AccountCategory accountCategory) {
        this.accountCategory = accountCategory;
    }

    public Double getWaterSaleBalance() {
        return waterSaleBalance;
    }

    public void setWaterSaleBalance(Double waterSaleBalance) {
        this.waterSaleBalance = waterSaleBalance;
    }

    public Double getMeterRentBalance() {
        return meterRentBalance;
    }

    public void setMeterRentBalance(Double meterRentBalance) {
        this.meterRentBalance = meterRentBalance;
    }

    public Double getPenaltiesBalance() {
        return penaltiesBalance;
    }

    public void setPenaltiesBalance(Double penaltiesBalance) {
        this.penaltiesBalance = penaltiesBalance;
    }

    public Boolean getUpdateBalance() {
        return updateBalance;
    }

    public void setUpdateBalance(Boolean updateBalance) {
        this.updateBalance = updateBalance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", balanceBroughtForward=" + balanceBroughtForward +
                ", outstandingBalance=" + outstandingBalance +
                ", accNo='" + accNo + '\'' +
                ", accName='" + accName + '\'' +
                ", averageConsumption=" + averageConsumption +
                ", active=" + active +
                ", metered=" + metered +
                '}';
    }
}
