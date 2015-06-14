package ke.co.suncha.simba.aqua.models;

import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> Created on 6/14/15.
 */
@Entity
@Table(name = "sms_templates")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SMSTemplate extends SimbaBaseEntity implements Serializable {
    @Id
    @Column(name = "sms_template_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long smsTemplateId;

    @NotNull
    @Column(name = "name", unique = true, length = 100)
    private String name;

    @Column(name = "message", length = 500)
    private String message;
}