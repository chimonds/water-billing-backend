package ke.co.suncha.simba.aqua.makerChecker;

import ke.co.suncha.simba.admin.models.UserRole;
import ke.co.suncha.simba.aqua.makerChecker.type.TaskType;
import ke.co.suncha.simba.aqua.models.Location;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha on 11/16/16.
 */
@Entity
@Table(name = "task_approvals")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Approval implements Serializable {
    @Id
    @Column(name = "task_approval_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long approvalId;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_type_id")
    private TaskType taskType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_role_id")
    private UserRole userRole;

    @Column(name = "step_no")
    private Integer stepNo = 1;

    @Column(name = "approval_step")
    private Integer approvalStep = ApprovalStep.UNKNOWN;

    public Long getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(Long approvalId) {
        this.approvalId = approvalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public Integer getStepNo() {
        return stepNo;
    }

    public void setStepNo(Integer stepNo) {
        this.stepNo = stepNo;
    }

    public Integer getApprovalStep() {
        return approvalStep;
    }

    public void setApprovalStep(Integer approvalStep) {
        this.approvalStep = approvalStep;
    }
}
