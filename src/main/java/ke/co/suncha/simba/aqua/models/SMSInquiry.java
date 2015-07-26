package ke.co.suncha.simba.aqua.models;

import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha.manyala on 7/23/15.
 */
@Entity
@Table(name = "sms_inquiries")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SMSInquiry extends SimbaBaseEntity implements Serializable {
    @Id
    @Column(name = "sms_inquiry_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long smsInquiryId;

    @Column(name = "message", length = 255)
    private String message;

    @Column(name = "send")
    private Boolean send = false;

    @Column(name = "sequence_id", unique = true)
    private Integer sequenceId;

    @Column(name = "string_date")
    private String stringDate;

    @Column(name = "msg_from")
    private String msgFrom;

    @Column(name = "msg_to")
    private String msgTo;

    public long getSmsInquiryId() {
        return smsInquiryId;
    }

    public void setSmsInquiryId(long smsInquiryId) {
        this.smsInquiryId = smsInquiryId;
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

    public Integer getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Integer sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getStringDate() {
        return stringDate;
    }

    public void setStringDate(String stringDate) {
        this.stringDate = stringDate;
    }

    public String getMsgFrom() {
        return msgFrom;
    }

    public void setMsgFrom(String msgFrom) {
        this.msgFrom = msgFrom;
    }

    public String getMsgTo() {
        return msgTo;
    }

    public void setMsgTo(String msgTo) {
        this.msgTo = msgTo;
    }
}
