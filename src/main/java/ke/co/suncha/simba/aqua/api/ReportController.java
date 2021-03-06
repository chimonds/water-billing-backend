package ke.co.suncha.simba.aqua.api;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.reports.AccountsReportRequest;
import ke.co.suncha.simba.aqua.reports.ReportsParam;
import ke.co.suncha.simba.aqua.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public RestResponse getPayments(@RequestBody RestRequestObject<AccountsReportRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportService.getPayments(requestObject);
    }

    @RequestMapping(value = "/accountsToActivate", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list of accounts to activate.", notes = "The list is not paginated.")
    public RestResponse getAccountsToActivate(@RequestBody RestRequestObject<AccountsReportRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportService.getAccountsToActivate(requestObject);
    }

    @RequestMapping(value = "/billingCharges", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list of payments.", notes = "The list is not paginated.")
    public RestResponse getBillingChecklistCharges(@RequestBody RestRequestObject<AccountsReportRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportService.getDetailedBillingCheckList(requestObject);
    }

    @RequestMapping(value = "/statement/{id}", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get an account statement.", notes = "The list is not paginated.")
    public RestResponse getAccountStatement(@ApiParam(value = "The ID of the existing account resource.", required = true) @PathVariable("id") Long accountId, @RequestBody RestRequestObject<ReportsParam> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportService.getAccountStatement(requestObject, accountId);
    }

    @RequestMapping(value = "/billingSummary", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list billing summary items.", notes = "The list is not paginated.")
    public RestResponse getBillingSummary(@RequestBody RestRequestObject<ReportsParam> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportService.getBillingSummary(requestObject);
    }

    @RequestMapping(value = "/waris", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list waris summary items.", notes = "The list is not paginated.")
    public RestResponse getWaris(@RequestBody RestRequestObject<AccountsReportRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportService.getWaris(requestObject);
    }


    @RequestMapping(value = "/warisByAccountCategories", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list waris summary items.", notes = "The list is not paginated.")
    public RestResponse getWarisByAccountCategories(@RequestBody RestRequestObject<AccountsReportRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportService.getWarisByAccountCategories(requestObject);
    }

    @RequestMapping(value = "/potentialCutOff", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list of accounts.", notes = "The list is not paginated.")
    public RestResponse getPotentialCutOff(@RequestBody RestRequestObject<ReportsParam> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportService.getPotentialCutOff(requestObject);
    }

    @RequestMapping(value = "/accountsNotBilled", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list of accounts.", notes = "The list is not paginated.")
    public RestResponse getAccountsNotBilled(@RequestBody RestRequestObject<ReportsParam> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportService.getAccountsNotBilled(requestObject);
    }

    @RequestMapping(value = "/consumersWithoutPhones", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list of accounts.", notes = "The list is not paginated.")
    public RestResponse getConsumersWithoutPhones(@RequestBody RestRequestObject<ReportsParam> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportService.getConsumersWithoutPhoneNumbers(requestObject);
    }

    @RequestMapping(value = "/monthlyBills", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list of accounts.", notes = "The list is not paginated.")
    public RestResponse getMonthlyBills(@RequestBody RestRequestObject<ReportsParam> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return reportService.getMonthlyBills(requestObject);
    }

}
