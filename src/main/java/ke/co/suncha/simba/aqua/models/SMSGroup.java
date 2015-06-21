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

    @Column(name = "contacts", length = 1000)
    private String contacts;

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "sms_group_zones")
    private List<Zone> zones;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "sms_template_id")
    private SMSTemplate smsTemplate;

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
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
}
