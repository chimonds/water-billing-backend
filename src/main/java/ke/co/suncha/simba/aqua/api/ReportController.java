package ke.co.suncha.simba.aqua.api;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.reports.ReportsParam;
import ke.co.suncha.simba.aqua.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> on 5/6/15.
 */
@RestController
@RequestMapping(value = "/api/v1/reports")
@Api(value = "Reports", description = "Reports API")
public class ReportController extends AbstractRestHandler {
    @Autowired
    private ReportService reportService;

    @RequestMapping(value = "/payments", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list of payments.", notes = "The list is not paginated.")
    public RestResponse getPaymentsReport(@RequestBody RestRequestObject<ReportsParam> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportService.getPaymentsReport(requestObject);
    }

}
