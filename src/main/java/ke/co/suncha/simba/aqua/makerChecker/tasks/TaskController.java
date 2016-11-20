package ke.co.suncha.simba.aqua.makerChecker.tasks;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.makerChecker.type.TaskType;
import ke.co.suncha.simba.aqua.makerChecker.type.TaskTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by maitha on 11/20/16.
 */
@RestController
@RequestMapping(value = "/api/v1/tasks")
public class TaskController extends AbstractRestHandler {
    @Autowired
    private TaskService taskService;

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ApiOperation(value = "Get a paginated list of all task types.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse getAll(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return taskService.getAllByFilter(requestObject);
    }

    @RequestMapping(value = "/one", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public RestResponse findOne(@RequestBody RestRequestObject<Task> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return taskService.getOne(requestObject);
    }
}
