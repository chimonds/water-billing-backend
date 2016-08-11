package ke.co.suncha.simba.aqua.reports.scheduled;

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
 * Created by maitha.manyala on 8/1/16.
 */
@RestController
@RequestMapping(value = "/api/v1/reportHeaders")
public class ReportHeaderController extends AbstractRestHandler {
    @Autowired
    private ReportHeaderService reportHeaderService;

    @RequestMapping(value = "/accountBalances", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse createAccountBalancesRequest(@RequestBody RestRequestObject<ReportHeader> requestObject) {
        return this.reportHeaderService.createAccountBalancesRequest(requestObject);
    }

    @RequestMapping(value = "/createAgeing", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse createAgeingBalancesRequest(@RequestBody RestRequestObject<ReportHeader> requestObject) {
        return this.reportHeaderService.createAgeingBalancesRequest(requestObject);
    }

    @RequestMapping(value = "/ageingPage", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    public RestResponse getAgeingPage(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportHeaderService.getAgeingBalancesHeaderList(requestObject);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    public RestResponse getAll(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportHeaderService.getAccountBalancesHeaderList(requestObject);
    }

    @RequestMapping(value = "accountBalancesReport", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    public RestResponse getAccountBalancesReport(@RequestBody RestRequestObject<ReportHeader> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportHeaderService.getAccountBalancesReport(requestObject);
    }

    @RequestMapping(value = "ageingReport", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    public RestResponse ageingReport(@RequestBody RestRequestObject<ReportHeader> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportHeaderService.getAgeingBalancesReport(requestObject);
    }
}
