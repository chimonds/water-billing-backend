package ke.co.suncha.simba.aqua.sms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ke.co.suncha.simba.aqua.account.Account;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha.manyala on 7/27/17.
 */
@Entity
@Table(name = "sms_inbox")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SMSInbox implements Serializable {
    @Id
    @Column(name = "sms_inbox_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long smsInquiryId;

    @Column(name = "sms_from", length = 30)
    private String from;

    @Column(name = "sms_to", length = 30)
    private String to;

    @Column(name = "msg", length = 1000)
    private String text;

    @Column(name = "date_received")
    private String date;

    @Column(name = "sms_id")
    private String id;

    @Column(name = "link_id")
    private String linkId;

    @Column(name = "is_replied")
    private Boolean replied = Boolean.FALSE;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_replied")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime dateReplied = new DateTime();

    @Column(name = "reply_msg")
    private String replyMsg;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    public Long getSmsInquiryId() {
        return smsInquiryId;
    }

    public void setSmsInquiryId(Long smsInquiryId) {
        this.smsInquiryId = smsInquiryId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public Boolean getReplied() {
        return replied;
    }

    public void setReplied(Boolean replied) {
        this.replied = replied;
    }

    public DateTime getDateReplied() {
        return dateReplied;
    }

    public void setDateReplied(DateTime dateReplied) {
        this.dateReplied = dateReplied;
    }

    public String getReplyMsg() {
        return replyMsg;
    }

    public void setReplyMsg(String replyMsg) {
        this.replyMsg = replyMsg;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
