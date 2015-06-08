package ke.co.suncha.simba.aqua.api;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.models.MPESATransaction;
import ke.co.suncha.simba.aqua.models.Meter;
import ke.co.suncha.simba.aqua.services.MPESAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> Created on 6/8/15.
 */
@RestController
@RequestMapping(value = "/api/v1/mpesa")
@Api(value = "MPESA", description = "MPESA transactions API")
public class MPESATransactionsController extends AbstractRestHandler {
    @Autowired
    private MPESAService mpesaService;

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a paginated list of all mpesa transactions.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse getAllByFilter(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return mpesaService.getAllByFilter(requestObject);
    }

    @RequestMapping(value = "allocate/{id}", method = RequestMethod.PUT, consumes = { "application/json", "application/xml" }, produces = { "application/json", "application/xml" })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Update MPESA transaction account number.", notes = "You have to provide a valid user record ID in the URL. The ID attribute can not be updated.")
    public RestResponse allocation(@ApiParam(value = "The ID of the existing mpesa transactions resource.", required = true) @PathVariable("id") Long id, @RequestBody RestRequestObject<MPESATransaction> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return mpesaService.allocate(requestObject, id);
    }
}
