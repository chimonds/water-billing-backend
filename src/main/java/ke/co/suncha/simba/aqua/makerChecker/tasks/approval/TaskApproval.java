package ke.co.suncha.simba.aqua.makerChecker.tasks.approval;

import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.aqua.makerChecker.Approval;
import ke.co.suncha.simba.aqua.makerChecker.tasks.Task;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha on 11/21/16.
 */
@Entity
@Table(name = "tasks_approvals")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskApproval implements Serializable {
    @Id
    @Column(name = "task_approval_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long taskApprovalId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approval_id")
    private Approval approval;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "notes")
    private String notes;

    @Column(name = "action")
    private Integer action = TaskAction.UNKNOWN;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    public Long getTaskApprovalId() {
        return taskApprovalId;
    }

    public void setTaskApprovalId(Long taskApprovalId) {
        this.taskApprovalId = taskApprovalId;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Approval getApproval() {
        return approval;
    }

    public void setApproval(Approval approval) {
        this.approval = approval;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }
}
