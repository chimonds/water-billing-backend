package ke.co.suncha.simba.aqua.makerChecker.tasks;

import com.mysema.query.BooleanBuilder;
import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
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
import ke.co.suncha.simba.aqua.makerChecker.type.TaskType;
import ke.co.suncha.simba.aqua.makerChecker.type.TaskTypeRepository;
import ke.co.suncha.simba.aqua.makerChecker.type.TaskTypeService;
import ke.co.suncha.simba.aqua.models.Account;
import ke.co.suncha.simba.aqua.models.Bill;
import ke.co.suncha.simba.aqua.models.Consumer;
import ke.co.suncha.simba.aqua.services.AccountService;
import ke.co.suncha.simba.aqua.services.BillService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

/**
 * Created by maitha on 11/20/16.
 */
@Service
@Transactional
public class TaskService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    EntityManager entityManager;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuthManager authManager;

    @Autowired
    SystemActionService systemActionService;

    @Autowired
    UserService userService;

    @Autowired
    AccountService accountService;

    @Autowired
    TaskTypeService taskTypeService;

    @Autowired
    BillService billService;

    @Autowired
    ApprovalService approvalService;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public Boolean canAdd(Long accountId, String taskTypeName) {
        Boolean add = Boolean.FALSE;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QTask.task.account.accountId.eq(accountId));
        builder.and(QTask.task.taskType.name.eq(taskTypeName));
        Task task = taskRepository.findOne(builder);
        if (task != null) {
            if (task.getApprovalStep() == ApprovalStep.CANCEL || task.getApprovalStep() == ApprovalStep.DECLINE) {
                add = Boolean.TRUE;
            }
        } else {
            add = Boolean.TRUE;
        }
        return add;
    }

    public Task create(Long accountId, String notes, String taskTypeName, String emailAddress, Double amount, Long recordId) {
        //set user
        Task task = new Task();
        task.setAccount(accountService.getByAccountId(accountId));
        task.setTaskType(taskTypeService.getByName(taskTypeName));
        task.setApprovalStep(ApprovalStep.START);
        task.setAmount(amount);
        task.setRecordId(recordId);
        task.setUser(userService.getByEmailAddress(emailAddress));
        task.setNotes(notes);
        Approval approval = approvalService.getStart(task.getTaskType().getTaskTypeId());
        if (approval != null) {
            task.setApproval(approval);
        }
        task = taskRepository.save(task);
        String sno = StringUtils.leftPad(task.getTaskId().toString(), 3, "0");
        task.setSno(sno);
        return taskRepository.save(task);
    }

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    public RestResponse getOne(RestRequestObject<Task> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "approval_tasks_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Task task = taskRepository.findOne(requestObject.getObject().getTaskId());
                if (task != null) {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(task);
                    response = new RestResponse(responseObject, HttpStatus.OK);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(task.getTaskId()));
                    auditRecord.setParentObject("TASKS");
                    auditRecord.setCurrentData(task.toString());
                    auditRecord.setNotes("VIEW TASK");
                    auditService.log(AuditOperation.VIEWED, auditRecord);
                    //End - audit trail

                } else {
                    responseObject.setMessage("Your search did not match any records");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "approval_tasks_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest p = requestObject.getObject();
                BooleanBuilder builder = new BooleanBuilder();

                if (!p.getFilter().isEmpty()) {

                }

                Page<Task> page = taskRepository.findAll(builder, new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(page);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @PostConstruct
    public void init() {
        systemActionService.create("approval_tasks_view");
    }
}