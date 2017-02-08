package ke.co.suncha.simba.aqua.models;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha.manyala on 2/8/17.
 */
@Entity
@Table(name = "executive_contacts")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ExecutiveContact implements Serializable {
    @Id
    @Column(name = "contact_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long contactId;

    @Column(name = "phone_no")
    private String phoneNo;

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
}