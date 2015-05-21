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
    @Column(name = "operation")
    private String operation;

    @Column(name = "parent_id")
    private String parentID;

    @Column(name = "parent_object")
    private String parentObject;


    @Column(name = "previous_data", length = 3000)
    private String previousDate;

    @Column(name = "current_data", length = 3000)
    private String currentData;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "author", length = 150)
    private String author;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getParentObject() {
        return parentObject;
    }

    public void setParentObject(String parentObject) {
        this.parentObject = parentObject;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getPreviousDate() {
        return previousDate;
    }

    public void setPreviousDate(String previousDate) {
        this.previousDate = previousDate;
    }

    public String getCurrentData() {
        return currentData;
    }

    public void setCurrentData(String currentData) {
        this.currentData = currentData;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
