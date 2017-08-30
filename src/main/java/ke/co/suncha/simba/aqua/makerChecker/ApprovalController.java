package ke.co.suncha.simba.aqua.makerChecker;

import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.makerChecker.type.TaskType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by maitha on 11/18/16.
 */
@RestController
@RequestMapping(value = "/api/v1/approvals")
public class ApprovalController extends AbstractRestHandler {
    @Autowired
    ApprovalService approvalService;

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse create(@RequestBody RestRequestObject<Approval> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return approvalService.create(requestObject);
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse remove(@RequestBody RestRequestObject<Approval> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return approvalService.remove(requestObject);
    }

    @RequestMapping(value = "/byTaskType", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public RestResponse findOne(@RequestBody RestRequestObject<TaskType> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return approvalService.getAll(requestObject);
    }
}
