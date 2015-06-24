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
 * Created by manyala on 6/17/15.
 */
@Entity
@Table(name = "sms_groups")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SMSGroup extends SimbaBaseEntity implements Serializable {
    @Id
    @Column(name = "sms_group_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long smsGroupId;

    @NotNull
    @Column(name = "name", unique = true, length = 255)
    private String name;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "messages_sent")
    private Integer messagesSent = 0;

    @Column(name = "all_messages")
    private Integer messages = 0;

    @Column(name = "from_system")
    private Boolean fromSystem=false;


    @Column(name = "approved")
    private Boolean approved = false;

    @Column(name = "approval_status")
    private String status = "Pending Approval";

    @Column(name = "sms_populated")
    private Boolean exploded = false;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "sms_group_zones")
    private List<Zone> zones;

    @OneToMany(mappedBy = "smsGroup", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Contact> contacts;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "sms_template_id")
    private SMSTemplate smsTemplate;

    @JsonIgnore
    @OneToMany(mappedBy = "smsGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SMS> smsList;

    public Boolean getFromSystem() {
        return fromSystem;
    }

    public void setFromSystem(Boolean fromSystem) {
        this.fromSystem = fromSystem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getMessagesSent() {
        return messagesSent;
    }

    public void setMessagesSent(Integer messagesSent) {
        this.messagesSent = messagesSent;
    }

    public Integer getMessages() {
        return messages;
    }

    public void setMessages(Integer messages) {
        this.messages = messages;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public Boolean getExploded() {
        return exploded;
    }

    public void setExploded(Boolean exploded) {
        this.exploded = exploded;
    }

    public List<SMS> getSmsList() {
        return smsList;
    }

    public void setSmsList(List<SMS> smsList) {
        this.smsList = smsList;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public long getSmsGroupId() {
        return smsGroupId;
    }

    public void setSmsGroupId(long smsGroupId) {
        this.smsGroupId = smsGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Zone> getZones() {
        return zones;
    }

    public void setZones(List<Zone> zones) {
        this.zones = zones;
    }

    public SMSTemplate getSmsTemplate() {
        return smsTemplate;
    }

    public void setSmsTemplate(SMSTemplate smsTemplate) {
        this.smsTemplate = smsTemplate;
    }

    @Override
    public String toString() {
        return "SMSGroup{" +
                "smsGroupId=" + smsGroupId +
                ", name='" + name + '\'' +
                ", notes='" + notes + '\'' +
                ", zones=" + zones +
                ", contacts=" + contacts +
                ", smsTemplate=" + smsTemplate +
                '}';
    }
}
