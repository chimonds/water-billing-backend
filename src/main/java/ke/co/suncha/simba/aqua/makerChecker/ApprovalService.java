package ke.co.suncha.simba.aqua.makerChecker;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.models.UserRole;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SystemActionService;
import ke.co.suncha.simba.admin.service.UserRoleService;
import ke.co.suncha.simba.aqua.makerChecker.type.QTaskType;
import ke.co.suncha.simba.aqua.makerChecker.type.TaskType;
import ke.co.suncha.simba.aqua.makerChecker.type.TaskTypeService;
import ke.co.suncha.simba.aqua.models.Location;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maitha on 11/16/16.
 */
@Service
@Transactional
public class ApprovalService {
    @Autowired
    EntityManager entityManager;

    @Autowired
    ApprovalRepository approvalRepository;

    @Autowired
    TaskTypeService taskTypeService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuthManager authManager;

    @Autowired
    SystemActionService systemActionService;

    @Autowired
    UserRoleService userRoleService;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public RestResponse getAll(RestRequestObject<TaskType> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "task_types_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                TaskType tt = requestObject.getObject();
                List<Approval> approvals = this.getByTaskType(tt.getTaskTypeId());
                responseObject.setMessage("Task types fetched successfully");
                responseObject.setPayload(approvals);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
        }
        return response;
    }

    @Transactional
    public RestResponse create(RestRequestObject<Approval> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "task_types_create");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Approval approval = requestObject.getObject();
                //what to check

                if (StringUtils.isEmpty(approval.getName())) {
                    responseObject.setMessage("Approval step can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                if (approval.getUserRole() == null) {
                    responseObject.setMessage("Approval user role can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                UserRole userRole = userRoleService.getOne(approval.getUserRole().getUserRoleId());
                if (userRole == null) {
                    responseObject.setMessage("Invalid user role resource");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }
                approval.setUserRole(userRole);

                if (approval.getTaskType() == null) {
                    responseObject.setMessage("Task type can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                TaskType taskType = taskTypeService.getById(approval.getTaskType().getTaskTypeId());
                if (taskType == null) {
                    responseObject.setMessage("Invalid task type resource");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                BooleanBuilder builder = new BooleanBuilder();
                builder.and(QApproval.approval.name.eq(approval.getName()));
                builder.and(QApproval.approval.taskType.taskTypeId.eq(approval.getTaskType().getTaskTypeId()));
                if (approvalRepository.count(builder) > 0) {
                    responseObject.setMessage("Approval step name already exists");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                builder = new BooleanBuilder();
                builder.and(QApproval.approval.taskType.taskTypeId.eq(approval.getTaskType().getTaskTypeId()));
                builder.and(QApproval.approval.userRole.userRoleId.eq(userRole.getUserRoleId()));
                if (approvalRepository.count(builder) > 0) {
                    responseObject.setMessage("User role already exists with the steps");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                builder = new BooleanBuilder();
                builder.and(QApproval.approval.taskType.taskTypeId.eq(approval.getTaskType().getTaskTypeId()));
                if (approvalRepository.count(builder) == 0) {
                    approval.setApprovalStep(ApprovalStep.START);
                    approval.setStepNo(1);
                }

                //allow only one start step
                if (approval.getApprovalStep() == ApprovalStep.START) {
                    builder = new BooleanBuilder();
                    builder.and(QApproval.approval.approvalStep.eq(ApprovalStep.START));
                    builder.and(QApproval.approval.taskType.taskTypeId.eq(approval.getTaskType().getTaskTypeId()));
                    if (approvalRepository.count(builder) == 1) {
                        responseObject.setMessage("You can only have one START step");
                        response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                        return response;
                    }
                }

                //allow only one Finish step
                if (approval.getApprovalStep() == ApprovalStep.END) {
                    builder = new BooleanBuilder();
                    builder.and(QApproval.approval.approvalStep.eq(ApprovalStep.END));
                    builder.and(QApproval.approval.taskType.taskTypeId.eq(approval.getTaskType().getTaskTypeId()));
                    if (approvalRepository.count(builder) == 1) {
                        responseObject.setMessage("You can only have one END step");
                        response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                        return response;
                    }
                }

                //Auto generate the step number
                if (approval.getApprovalStep() != ApprovalStep.START) {
                    builder = new BooleanBuilder();
                    builder.and(QApproval.approval.taskType.taskTypeId.eq(taskType.getTaskTypeId()));
                    JPAQuery query = new JPAQuery(entityManager);
                    Integer max = query.from(QApproval.approval).where(builder).singleResult(QApproval.approval.stepNo.max());
                    if (max != null) {
                        max = max + 1;
                        approval.setStepNo(max);
                    }
                }

                approval = create(approval, approval.getTaskType().getTaskTypeId());

                // package response
                responseObject.setMessage("Approval step created successfully. ");
                responseObject.setPayload(approval);
                response = new RestResponse(responseObject, HttpStatus.CREATED);

                //Start - audit trail
                AuditRecord auditRecord = new AuditRecord();
                auditRecord.setParentID(String.valueOf(approval.getApprovalId()));
                auditRecord.setParentObject("Approval");
                auditRecord.setNotes("CREATED APPROVAL STEP");
                auditService.log(AuditOperation.CREATED, auditRecord);
                //End - audit trail
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            ex.printStackTrace();
        }
        return response;
    }

    @Transactional
    public RestResponse remove(RestRequestObject<Approval> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "task_types_create");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Approval approval = requestObject.getObject();

                //check if exist
                Approval dbApproval = approvalRepository.findOne(approval.getApprovalId());
                if (dbApproval == null) {
                    responseObject.setMessage("Invalid approval step");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                //what to check
                BooleanBuilder builder = new BooleanBuilder();
                builder = new BooleanBuilder();
                builder.and(QApproval.approval.taskType.taskTypeId.eq(dbApproval.getTaskType().getTaskTypeId()));
                JPAQuery query = new JPAQuery(entityManager);
                Integer max = query.from(QApproval.approval).where(builder).singleResult(QApproval.approval.stepNo.max());
                if (max != dbApproval.getStepNo()) {
                    responseObject.setMessage("Sorry you only remove the last approval step");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }

                approvalRepository.delete(dbApproval);

                // package response
                responseObject.setMessage("Approval step removed successfully. ");
                responseObject.setPayload(approval);
                response = new RestResponse(responseObject, HttpStatus.CREATED);

            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            ex.printStackTrace();
        }
        return response;
    }

    private Approval create(Approval approval, Long taskTypeId) {
        TaskType taskType = taskTypeService.getById(taskTypeId);
        approval.setTaskType(taskType);
        approval = approvalRepository.save(approval);
        return approval;
    }

    //list by task type
    private List<Approval> getByTaskType(Long taskTypeId) {
        List<Approval> approvals = new ArrayList<>();
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QApproval.approval.taskType.taskTypeId.eq(taskTypeId));
        Iterable<Approval> iterable = approvalRepository.findAll(builder, new Sort(Sort.Direction.ASC, "stepNo"));
        for (Approval approval : iterable) {
            approvals.add(approval);
        }
        return approvals;
    }
}
