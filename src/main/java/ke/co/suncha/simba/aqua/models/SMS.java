package ke.co.suncha.simba.aqua.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Calendar;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> Created on 5/26/15.
 */
@Entity
@Table(name = "sms")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SMS extends SimbaBaseEntity implements Serializable {
    @Id
    @Column(name = "sms_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long smsId;

    @Column(name = "message", length = 255)
    private String message;

    @Column(name = "send")
    private Boolean send = false;

    @Column(name = "re_send")
    private Boolean reSend = Boolean.FALSE;

    @Column(name = "is_void")
    private Boolean isVoid = false;

    @Column(name = "sms_cost")
    private Double cost = 0d;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_send")
    private Calendar dateSend = Calendar.getInstance();

    @NotNull
    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "response")
    private String response = "";

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sms_group_id")
    private SMSGroup smsGroup;

    public SMSGroup getSmsGroup() {
        return smsGroup;
    }

    public void setSmsGroup(SMSGroup smsGroup) {
        this.smsGroup = smsGroup;
    }

    public long getSmsId() {
        return smsId;
    }

    public void setSmsId(long smsId) {
        this.smsId = smsId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSend() {
        return send;
    }

    public void setSend(Boolean send) {
        this.send = send;
    }

    public Calendar getDateSend() {
        return dateSend;
    }

    public void setDateSend(Calendar dateSend) {
        this.dateSend = dateSend;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Boolean getIsVoid() {
        if (this.isVoid == null) {
            this.isVoid = false;
        }
        return isVoid;
    }

    public void setIsVoid(Boolean isVoid) {
        this.isVoid = isVoid;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Boolean getReSend() {
        return reSend;
    }

    public void setReSend(Boolean reSend) {
        this.reSend = reSend;
    }

    @Override
    public String toString() {
        return "SMS{" +
                "smsId=" + smsId +
                ", message='" + message + '\'' +
                ", send=" + send +
                ", isVoid=" + isVoid +
                ", dateSend=" + dateSend +
                ", mobileNumber='" + mobileNumber + '\'' +
                '}';
    }
}
