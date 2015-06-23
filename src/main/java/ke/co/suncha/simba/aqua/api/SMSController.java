package ke.co.suncha.simba.aqua.api;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.models.Account;
import ke.co.suncha.simba.aqua.models.SMSGroup;
import ke.co.suncha.simba.aqua.models.SMSTemplate;
import ke.co.suncha.simba.aqua.services.SMSService;
import ke.co.suncha.simba.aqua.services.SMSTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by manyala on 6/21/15.
 */
@RestController
@RequestMapping(value = "/api/v1/sms")
@Api(value = "SMS", description = "SMS API")
public class SMSController extends AbstractRestHandler {
    @Autowired
    private SMSService smsService;

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a SMS resource.", notes = "Returns the URL of the new resource in the Location header.")
    public RestResponse createUser(@RequestBody RestRequestObject<SMSGroup> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return this.smsService.create(requestObject);
    }

    @RequestMapping(value = "/groups", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a paginated list of all SMS groups.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse getAllSMSGroups(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return smsService.getAllSMSGroupsByFilter(requestObject);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a paginated list of all SMSs.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse getAllSMS(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return smsService.getAllSMSByFilter(requestObject);
    }

    @RequestMapping(value = "/approve/{id}", method = RequestMethod.PUT, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Approve/Reject sms group resource.", notes = "Returns the URL of the new resource in the Location header.")
    public RestResponse create(@ApiParam(value = "The ID of the existing consumer resource.", required = true) @PathVariable("id") Long id, @RequestBody RestRequestObject<SMSGroup> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return this.smsService.approve(requestObject, id);
    }
}
