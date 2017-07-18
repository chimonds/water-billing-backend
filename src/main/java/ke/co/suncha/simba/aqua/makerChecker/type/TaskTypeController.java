package ke.co.suncha.simba.aqua.makerChecker.type;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by maitha on 11/18/16.
 */
@RestController
@RequestMapping(value = "/api/v1/taskTypes")
@Api(value = "Approval task types", description = "Task types API")
public class TaskTypeController extends AbstractRestHandler {
    @Autowired
    private TaskTypeService taskTypeService;

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ApiOperation(value = "Get a paginated list of all task types.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse getAll(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return taskTypeService.getAllByFilter(requestObject);
    }

    @RequestMapping(value = "/one", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public RestResponse findOne(@RequestBody RestRequestObject<TaskType> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return taskTypeService.getOne(requestObject);
    }
}
