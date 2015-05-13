package ke.co.suncha.simba.admin.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by manyala on 5/12/15.
 */
@Entity
@Table(name = "audit_records")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonSerialize
public class AuditRecord extends SimbaBaseEntity implements Serializable {
    @Id
    @NotNull
    @Column(name = "record_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long recordId;

    @NotNull
    @Column(name = "user_action")
    private String action;

    @Column(name = "ref_no")
    private String refNo;

    @Column(name = "data_ago")
    private String ago;

    @Column(name = "data_now")
    private String dataNow;

}
