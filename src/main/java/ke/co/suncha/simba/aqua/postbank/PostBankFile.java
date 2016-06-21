package ke.co.suncha.simba.aqua.postbank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ke.co.suncha.simba.admin.models.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by maitha.manyala on 6/21/16.
 */
@Entity
@Table(name = "post_bank_files")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PostBankFile implements Serializable {
    @Id
    @Column(name = "file_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long fileId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "file_size", nullable = false)
    private Long size = 0l;

    @Column(name = "amount", nullable = false)
    private Double amount = 0d;

    @Column(name = "file_status")
    private Integer status = 0;

    @Column(name = "is_valid")
    private Boolean isValid = Boolean.FALSE;

    @Column(name = "line_count")
    private Integer lineCount = 0;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar createdOn = Calendar.getInstance();

    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    private Calendar lastModifiedDate = Calendar.getInstance();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @JsonIgnore
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @JsonIgnore
    private User lastModifiedBy;

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public Calendar getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Calendar createdOn) {
        this.createdOn = createdOn;
    }

    public Calendar getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Calendar lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Integer getLineCount() {
        return lineCount;
    }

    public void setLineCount(Integer lineCount) {
        this.lineCount = lineCount;
    }
}
