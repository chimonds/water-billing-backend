package ke.co.suncha.simba.aqua.api;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.models.SMSTemplate;
import ke.co.suncha.simba.aqua.models.Zone;
import ke.co.suncha.simba.aqua.services.SMSTemplateService;
import ke.co.suncha.simba.aqua.services.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by manyala on 6/15/15.
 */
@RestController
@RequestMapping(value = "/api/v1/smstemplate")
@Api(value = "SMS templates", description = "Connection zone API")
public class SMSTemplateController extends AbstractRestHandler {
    @Autowired
    private SMSTemplateService smsTemplateService;

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a SMS template resource.", notes = "Returns the URL of the new resource in the Location header.")
    public RestResponse createUser(@RequestBody RestRequestObject<SMSTemplate> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return this.smsTemplateService.create(requestObject);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a paginated list of all sms templates.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse getAll(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return smsTemplateService.getAllByFilter(requestObject);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Update sms template resource.", notes = "You have to provide a valid user role ID in the URL and in the payload. The ID attribute can not be updated.")
    public RestResponse update(@ApiParam(value = "The ID of the existing sms template resource.", required = true) @PathVariable("id") Long id, @RequestBody RestRequestObject<SMSTemplate> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return smsTemplateService.update(requestObject, id);
    }
}
