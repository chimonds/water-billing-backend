package ke.co.suncha.simba.aqua.makerChecker.tasks;

import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.models.UserRole;
import ke.co.suncha.simba.aqua.makerChecker.Approval;
import ke.co.suncha.simba.aqua.makerChecker.ApprovalStep;
import ke.co.suncha.simba.aqua.makerChecker.type.TaskType;
import ke.co.suncha.simba.aqua.models.Account;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha on 11/20/16.
 */
@Entity
@Table(name = "tasks")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Task implements Serializable {
    @Id
    @Column(name = "task_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long taskId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_type_id")
    private TaskType taskType;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approval_id")
    private Approval approval;

    @Column(name = "approval_step_id")
    private Integer approvalStep = ApprovalStep.START;

    @Column(name = "amount")
    private Double amount = 0d;

    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "notes",length = 1000)
    private String notes;

    @Column(name = "sno")
    private String sno;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastOn = new DateTime();

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public Approval getApproval() {
        return approval;
    }

    public void setApproval(Approval approval) {
        this.approval = approval;
    }

    public Integer getApprovalStep() {
        return approvalStep;
    }

    public void setApprovalStep(Integer approvalStep) {
        this.approvalStep = approvalStep;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public DateTime getLastOn() {
        return lastOn;
    }

    public void setLastOn(DateTime lastOn) {
        this.lastOn = lastOn;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }
}