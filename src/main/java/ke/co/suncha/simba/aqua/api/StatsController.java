package ke.co.suncha.simba.aqua.api;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.services.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by manyala on 5/19/15.
 */
@RestController
@RequestMapping(value = "/api/v1/stats")
@Api(value = "Stats", description = "Stats API")
public class StatsController extends AbstractRestHandler {
    @Autowired
    private StatsService statsService;

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a paginated list of all accounts.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse getStats(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return statsService.getStats(requestObject);
    }
}
