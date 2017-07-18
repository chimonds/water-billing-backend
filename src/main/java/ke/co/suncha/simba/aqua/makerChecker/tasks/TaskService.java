package ke.co.suncha.simba.aqua.makerChecker.tasks;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.models.UserRole;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SystemActionService;
import ke.co.suncha.simba.admin.service.UserRoleService;
import ke.co.suncha.simba.admin.service.UserService;
import ke.co.suncha.simba.admin.utils.Config;
import ke.co.suncha.simba.admin.utils.CustomPage;
import ke.co.suncha.simba.aqua.makerChecker.Approval;
import ke.co.suncha.simba.aqua.makerChecker.ApprovalService;
import ke.co.suncha.simba.aqua.makerChecker.ApprovalStep;
import ke.co.suncha.simba.aqua.makerChecker.type.TaskTypeConst;
import ke.co.suncha.simba.aqua.makerChecker.type.TaskTypeService;
import ke.co.suncha.simba.aqua.models.SMS;
import ke.co.suncha.simba.aqua.models.SMSGroup;
import ke.co.suncha.simba.aqua.repository.SMSGroupRepository;
import ke.co.suncha.simba.aqua.repository.SMSTemplateRepository;
import ke.co.suncha.simba.aqua.services.AccountService;
import ke.co.suncha.simba.aqua.billing.BillService;
import ke.co.suncha.simba.aqua.services.PaymentService;
import ke.co.suncha.simba.aqua.services.SMSService;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    PaymentService paymentService;

    @Autowired
    UserRoleService userRoleService;

    @Autowired
    SMSService smsService;

    @Autowired
    SMSGroupRepository smsGroupRepository;

    @Autowired
    SMSTemplateRepository smsTemplateRepository;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public Boolean canAdd(Long accountId, String taskTypeName) {
        Boolean add = Boolean.FALSE;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QTask.task.account.accountId.eq(accountId));
        builder.and(QTask.task.taskType.name.eq(taskTypeName));
        Task task = taskRepository.findOne(builder);
        if (task != null) {
            if (task.getApprovalStep() == ApprovalStep.REJECTED) {
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
        task = taskRepository.save(task);
        //add notification
        addNotification(task.getTaskId());
        return task;
    }

    @Transactional
    public void addNotification(Long taskId) {
        Task task = taskRepository.findOne(taskId);
        if (task == null) {
            return;
        }
        if (task.getApprovalStep() != ApprovalStep.REJECTED && task.getApprovalStep() != ApprovalStep.COMPLETED) {
            Iterable<User> users = userService.getByUserRole(task.getApproval().getUserRole().getUserRoleId());
            for (User user : users) {
                if (StringUtils.isNotEmpty(user.getMobileNo())) {
                    //$firstname
                    //$task_name
                    //$sno
                    SMSGroup smsGroup = smsGroupRepository.findByName(Config.SMS_NOTIFICATION_APPROVAL_TASK);
                    String msg = smsTemplateRepository.findByName(Config.SMS_TEMPLATE_APPROVAL_TASK).getMessage();
                    msg = StringUtils.replace(msg, "$firstname", user.getFirstName());
                    msg = StringUtils.replace(msg, "$task_name", task.getTaskType().getName());
                    msg = StringUtils.replace(msg, "$sno", task.getSno());
                    msg = StringUtils.replace(msg, "$acc_no", task.getAccount().getAccNo());

                    //save sms
                    SMS sms = new SMS();
                    sms.setSmsGroup(smsGroup);
                    sms.setMobileNumber(user.getMobileNo());
                    sms.setMessage(msg);
                    smsService.save(sms);
                }
            }
        }
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
                    User user = userService.getByEmailAddress(authManager.getEmailFromToken(requestObject.getToken()));
                    task.setEdit(canEdit(user, task.getTaskId()));
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

    private Boolean canEdit(User user, Long taskId) {
        Task dbTask = taskRepository.findOne(taskId);
        Boolean canEdit = Boolean.FALSE;
        if (user.getUserRole() == dbTask.getApproval().getUserRole()) {
            canEdit = Boolean.TRUE;
            if (user == dbTask.getUser()) {
                canEdit = Boolean.FALSE;
            }
            if (dbTask.getApprovalStep() == ApprovalStep.COMPLETED || dbTask.getApprovalStep() == ApprovalStep.REJECTED) {
                canEdit = Boolean.FALSE;
            }
        }
        return canEdit;
    }

    public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "approval_tasks_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                User user = userService.getByEmailAddress(authManager.getEmailFromToken(requestObject.getToken()));

                RestPageRequest p = requestObject.getObject();
                BooleanBuilder builder = new BooleanBuilder();

                if (!p.getFilter().isEmpty()) {
                    builder.or(QTask.task.account.accNo.containsIgnoreCase(p.getFilter()));
                    builder.or(QTask.task.sno.containsIgnoreCase(p.getFilter()));
                }

                Page<Task> page = taskRepository.findAll(builder, new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                CustomPage customPage = new CustomPage();
                if (page.hasContent()) {
                    List<Task> tasks = new ArrayList<>();
                    for (Task task : page.getContent()) {
                        Task dbTask = taskRepository.findOne(task.getTaskId());
                        dbTask.setEdit(canEdit(user, dbTask.getTaskId()));
                        tasks.add(dbTask);
                    }
                    customPage.setContent(tasks);
                    customPage.setTotalElements(page.getTotalElements());
                    customPage.setTotalPages(page.getTotalPages());
                }
                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(customPage);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public Task getById(Long taskId) {
        return taskRepository.findOne(taskId);
    }

    public Task save(Task task) {
        return taskRepository.save(task);
    }

    @PostConstruct
    public void init() {
        systemActionService.create("approval_tasks_view");
    }

    @Scheduled(fixedDelay = 5000)
    public void process() {
        try {
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(QTask.task.processed.eq(Boolean.FALSE));
            builder.and(QTask.task.approvalStep.eq(ApprovalStep.COMPLETED));
            Iterable<Task> tasks = taskRepository.findAll(builder);
            for (Task task : tasks) {
                postTransaction(task.getTaskId());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Transactional
    public void postTransaction(Long taskId) {
        Task task = taskRepository.findOne(taskId);
        if (task == null) {
            return;
        }
        if (StringUtils.equals(task.getTaskType().getName(), TaskTypeConst.DELETE_BILL)) {
            billService.delete(taskId);
        } else {
            paymentService.createFromTask(taskId);
        }
    }

    //second, minute, hour, day of month, month, day(s) of week
    //	0 0 0 1/1 * ? * every day at midnight
    //	0 0 0 1 1/1 ? * every first day of the month

    @Scheduled(cron = "0 30 8 * * MON-FRI")
    public void morningAlerts() {
        log.info("Morning alert Pending Tasks Alerts @::" + new DateTime());
        sendNotifications();
    }


    @Scheduled(cron = "0 30 14 * * MON-FRI")
    public void afterNoonAlerts() {
        log.info("After noon alert Pending Tasks Alerts @::" + new DateTime());
        sendNotifications();
    }

    private void sendNotifications() {
        List<Integer> pendingTasks = new ArrayList<>();
        pendingTasks.add(ApprovalStep.START);
        pendingTasks.add(ApprovalStep.ON_PROGRESS);
        pendingTasks.add(ApprovalStep.END);

        List<Long> roleIds = this.getPendingTasksRolesIds();
        if (!roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                BooleanBuilder builder = new BooleanBuilder();
                builder.and(QTask.task.processed.eq(Boolean.FALSE));
                builder.and(QTask.task.approval.userRole.userRoleId.eq(roleId));
                builder.and(QTask.task.approvalStep.in(pendingTasks));

                JPAQuery query = new JPAQuery(entityManager);
                Long count = query.from(QTask.task).where(builder).singleResult(QTask.task.count());
                log.info(count + " Tasks pending ");
                addPendingReminderNotification(roleId, count.intValue());
            }
        }

    }   

    @Transactional
    public void addPendingReminderNotification(Long roleId, Integer count) {
        UserRole userRole = userRoleService.getOne(roleId);

        if (userRole == null) {
            return;
        }

        Iterable<User> users = userService.getByUserRole(userRole.getUserRoleId());
        for (User user : users) {
            if (StringUtils.isNotEmpty(user.getMobileNo())) {
                //$firstname
                //$task_name
                //$sno
                SMSGroup smsGroup = smsGroupRepository.findByName(Config.SMS_NOTIFICATION_APPROVAL_TASK_REMINDER);
                String msg = smsTemplateRepository.findByName(Config.SMS_TEMPLATE_APPROVAL_TASK_REMINDER).getMessage();
                msg = StringUtils.replace(msg, "$firstname", user.getFirstName());
                msg = StringUtils.replace(msg, "$count", count.intValue() + "");


                //save sms
                SMS sms = new SMS();
                sms.setSmsGroup(smsGroup);
                sms.setMobileNumber(user.getMobileNo());
                sms.setMessage(msg);
                smsService.save(sms);

            }
        }
    }


    private List<Long> getPendingTasksRolesIds() {
        List<Long> roles = new ArrayList<>();
        List<Integer> pendingTasks = new ArrayList<>();
        pendingTasks.add(ApprovalStep.START);
        pendingTasks.add(ApprovalStep.ON_PROGRESS);
        pendingTasks.add(ApprovalStep.END);

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QTask.task.processed.eq(Boolean.FALSE));
        builder.and(QTask.task.approvalStep.in(pendingTasks));

        JPAQuery query = new JPAQuery(entityManager);
        List<Long> roleIds = query.from(QTask.task).where(builder).list(QTask.task.approval.userRole.userRoleId);
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                if (!roles.contains(roleId)) {
                    roles.add(roleId);
                }
            }
        }
        return roles;
    }
}