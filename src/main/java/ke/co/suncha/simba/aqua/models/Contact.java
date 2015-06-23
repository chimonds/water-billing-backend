package ke.co.suncha.simba.aqua.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by manyala on 6/21/15.
 */
@Entity
@Table(name = "sms_contacts")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Contact extends SimbaBaseEntity implements Serializable {
    @Id
    @Column(name = "contact_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long contactId;

    @NotNull
    @Column(name = "mobile")
    private String text;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "sms_group_id")
    private SMSGroup smsGroup;

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public SMSGroup getSmsGroup() {
        return smsGroup;
    }

    public void setSmsGroup(SMSGroup smsGroup) {
        this.smsGroup = smsGroup;
    }
}
