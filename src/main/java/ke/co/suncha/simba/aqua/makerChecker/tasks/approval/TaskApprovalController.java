package ke.co.suncha.simba.aqua.makerChecker.tasks.approval;

import com.wordnik.swagger.annotations.ApiOperation;
import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.makerChecker.Approval;
import ke.co.suncha.simba.aqua.makerChecker.ApprovalStep;
import ke.co.suncha.simba.aqua.makerChecker.tasks.Task;
import ke.co.suncha.simba.aqua.makerChecker.tasks.TaskService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by maitha on 11/21/16.
 */
@RestController
@RequestMapping(value = "/api/v1/taskApprovals")
public class TaskApprovalController extends AbstractRestHandler {
    @Autowired
    private TaskApprovalService taskApprovalService;

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    public RestResponse getAll(@RequestBody RestRequestObject<Task> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return taskApprovalService.getAllByFilter(requestObject);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse create(@RequestBody RestRequestObject<TaskApproval> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return taskApprovalService.create(requestObject);
    }
}
