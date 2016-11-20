package ke.co.suncha.simba.aqua.makerChecker.type;

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
import ke.co.suncha.simba.aqua.models.Account;
import ke.co.suncha.simba.aqua.models.AccountCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

/**
 * Created by maitha on 11/16/16.
 */
@Service
public class TaskTypeService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    EntityManager entityManager;

    @Autowired
    TaskTypeRepository taskTypeRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuthManager authManager;

    @Autowired
    SystemActionService systemActionService;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public TaskType getByName(String name) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QTaskType.taskType.name.eq(name));
        return taskTypeRepository.findOne(builder);
    }

    public TaskType getById(Long taskTypeId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QTaskType.taskType.taskTypeId.eq(taskTypeId));
        return taskTypeRepository.findOne(builder);
    }

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.ASC, "name");
    }

    public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "task_types_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                RestPageRequest p = requestObject.getObject();
                BooleanBuilder builder = new BooleanBuilder();
                if (!p.getFilter().isEmpty()) {
                    builder.and(QTaskType.taskType.name.containsIgnoreCase(p.getFilter()));
                }
                Page<TaskType> page = taskTypeRepository.findAll(builder, new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));

                if (page.hasContent()) {
                    responseObject.setMessage("Task types fetched successfully");
                    responseObject.setPayload(page);
                    response = new RestResponse(responseObject, HttpStatus.OK);
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

    public RestResponse getOne(RestRequestObject<TaskType> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "task_types_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                TaskType tt = requestObject.getObject();
                TaskType taskType = taskTypeRepository.findOne(tt.getTaskTypeId());

                if (taskType == null) {
                    responseObject.setMessage("Invalid task type");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                } else {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(taskType);
                    response = new RestResponse(responseObject, HttpStatus.OK);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(taskType.getTaskTypeId()));
                    auditRecord.setParentObject("TaskType");
                    auditRecord.setNotes("Task type view");
                    auditService.log(AuditOperation.VIEWED, auditRecord);
                    //End - audit trail
                }
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
        systemActionService.create("task_types_view");
        systemActionService.create("task_types_create");

        if (getByName("DELETE_BILL") == null) {
            TaskType taskType = new TaskType();
            taskType.setName("DELETE_BILL");
            taskType.setDescription("Delete a bill.");
            taskTypeRepository.save(taskType);
        }

        if (getByName("CREDIT_ADJUSTMENT") == null) {
            TaskType taskType = new TaskType();
            taskType.setName("CREDIT_ADJUSTMENT");
            taskType.setDescription("Make credit adjustments to account");
            taskTypeRepository.save(taskType);
        }

        if (getByName("DEBIT_ADJUSTMENT") == null) {
            TaskType taskType = new TaskType();
            taskType.setName("DEBIT_ADJUSTMENT");
            taskType.setDescription("Make debit adjustments to account");
            taskTypeRepository.save(taskType);
        }
    }
}
