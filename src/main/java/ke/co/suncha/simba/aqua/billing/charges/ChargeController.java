package ke.co.suncha.simba.aqua.billing.charges;

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
 * Created by maitha.manyala on 10/13/17.
 */
@RestController
@RequestMapping(value = "/api/v1/charges")
public class ChargeController extends AbstractRestHandler {
    @Autowired
    private ChargeManager chargeManager;

    @RequestMapping(value = "/create/{accountId}", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse create(@PathVariable("accountId") Long accountId, @RequestBody RestRequestObject<ChargeRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return chargeManager.create(requestObject, accountId);
    }


    @RequestMapping(value = "/delete", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse delete(@RequestBody RestRequestObject<Charge> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return chargeManager.delete(requestObject);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    public RestResponse getPage(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return chargeManager.getPage(requestObject);
    }
}