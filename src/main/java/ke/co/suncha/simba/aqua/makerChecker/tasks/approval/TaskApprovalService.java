package ke.co.suncha.simba.aqua.makerChecker.tasks.approval;

import com.mysema.query.BooleanBuilder;
import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SystemActionService;
import ke.co.suncha.simba.admin.service.UserService;
import ke.co.suncha.simba.aqua.makerChecker.Approval;
import ke.co.suncha.simba.aqua.makerChecker.ApprovalService;
import ke.co.suncha.simba.aqua.makerChecker.ApprovalStep;
import ke.co.suncha.simba.aqua.makerChecker.tasks.Task;
import ke.co.suncha.simba.aqua.makerChecker.tasks.TaskService;
import ke.co.suncha.simba.aqua.makerChecker.type.TaskTypeService;
import ke.co.suncha.simba.aqua.services.AccountManagerService;
import ke.co.suncha.simba.aqua.billing.BillService;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maitha on 11/21/16.
 */
@Transactional
@Service
public class TaskApprovalService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    EntityManager entityManager;

    @Autowired
    TaskApprovalRepository taskApprovalRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuthManager authManager;

    @Autowired
    SystemActionService systemActionService;

    @Autowired
    UserService userService;

    @Autowired
    AccountManagerService accountService;

    @Autowired
    TaskTypeService taskTypeService;

    @Autowired
    BillService billService;

    @Autowired
    ApprovalService approvalService;

    @Autowired
    TaskService taskService;


    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public RestResponse create(RestRequestObject<TaskApproval> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "approval_tasks_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                User user = userService.getByEmailAddress(authManager.getEmailFromToken(requestObject.getToken()));

                TaskApproval taskApproval = requestObject.getObject();
                if (taskApproval == null) {
                    responseObject.setMessage("Invalid task approval resource");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (taskApproval.getAction() == null) {
                    responseObject.setMessage("Approval action can not be empy");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (StringUtils.isEmpty(taskApproval.getNotes())) {
                    responseObject.setMessage("Approval notes can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                Task task = taskService.getById(taskApproval.getTask().getTaskId());
                if (task == null) {
                    responseObject.setMessage("Invalid approval task resource");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (task.getApprovalStep() == ApprovalStep.REJECTED || task.getApprovalStep() == ApprovalStep.COMPLETED) {
                    responseObject.setMessage("Invalid action. Task already rejected or completed");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (task.getApproval().getUserRole() != user.getUserRole()) {
                    responseObject.setMessage("You are not authorized to submit this request.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                taskApproval = create(task.getTaskId(), taskApproval.getNotes(), authManager.getEmailFromToken(requestObject.getToken()), taskApproval.getAction());

                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(taskApproval);
                response = new RestResponse(responseObject, HttpStatus.OK);

                //Start - audit trail
                AuditRecord auditRecord = new AuditRecord();
                auditRecord.setParentID(String.valueOf(taskApproval.getTaskApprovalId()));
                auditRecord.setParentObject("CREATE TASKS APPROVAL");
                auditRecord.setCurrentData(task.toString());
                auditRecord.setNotes("CREATE TASK APPROVAL");
                auditService.log(AuditOperation.VIEWED, auditRecord);
                //End - audit trail
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getAllByFilter(RestRequestObject<Task> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "approval_tasks_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Task task = taskService.getById(requestObject.getObject().getTaskId());
                BooleanBuilder builder = new BooleanBuilder();
                builder.and(QTaskApproval.taskApproval.task.taskId.eq(task.getTaskId()));
                Iterable<TaskApproval> iterable = taskApprovalRepository.findAll(builder);
                List<TaskApproval> taskApprovals = new ArrayList<>();
                for (TaskApproval taskApproval : iterable) {
                    taskApprovals.add(taskApproval);
                }
                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(taskApprovals);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public TaskApproval create(Long taskId, String notes, String emailAddress, Integer action) {
        //set user
        Task task = taskService.getById(taskId);
        TaskApproval taskApproval = new TaskApproval();
        taskApproval.setTask(task);
        taskApproval.setUser(userService.getByEmailAddress(emailAddress));
        taskApproval.setNotes(notes);
        taskApproval.setAction(action);
        taskApproval.setCreatedOn(new DateTime());
        taskApproval = taskApprovalRepository.save(taskApproval);

        if (action == TaskAction.REJECT) {
            task.setApprovalStep(ApprovalStep.REJECTED);
            task.setLastOn(new DateTime());
        } else if (action == TaskAction.APPROVE) {
            if (task.getApproval().getApprovalStep() == ApprovalStep.END) {
                task.setApprovalStep(ApprovalStep.COMPLETED);
            } else {
                Integer step = task.getApproval().getStepNo();
                step = step + 1;
                Approval approval = approvalService.getByStep(task.getTaskType().getTaskTypeId(), step);
                if (approval == null) {
                    task.setApprovalStep(ApprovalStep.COMPLETED);
                } else {
                    task.setApproval(approval);
                    task.setApprovalStep(approval.getApprovalStep());
                }
            }
        }

        task = taskService.save(task);

        //add notification
        taskService.addNotification(task.getTaskId());
        return taskApproval;
    }
}
