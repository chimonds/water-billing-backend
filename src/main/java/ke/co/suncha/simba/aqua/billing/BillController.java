/*
 * The MIT License
 *
 * Copyright 2015 Maitha Manyala.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ke.co.suncha.simba.aqua.billing;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.makerChecker.tasks.Task;
import ke.co.suncha.simba.aqua.reports.AccountsReportRequest;
import ke.co.suncha.simba.aqua.utils.BillRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@RestController
@RequestMapping(value = "/api/v1/bills")
@Api(value = "Account bills", description = "Account bills API")
public class BillController {
    @Autowired
    private BillService billService;

    @RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a paginated list of all connection locations.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse getAll(@ApiParam(value = "The ID of the existing consumer resource.", required = true) @PathVariable("id") Long account_id, @RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return billService.getAllByAccount(requestObject, account_id);
    }

    @RequestMapping(value = "last/{id}", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a paginated list of all connection locations.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse getLastBill(@ApiParam(value = "The ID of the existing consumer resource.", required = true) @PathVariable("id") Long accountId, @RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return billService.getLastBill(requestObject, accountId);
    }

    @RequestMapping(value = "delete/{id}", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a paginated list of all connection locations.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse deleteBill(@ApiParam(value = "The ID of the existing consumer resource.", required = true) @PathVariable("id") Long billId, @RequestBody RestRequestObject<Task> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return billService.deleteBill(requestObject, billId);
    }

    @RequestMapping(value = "transfer/{id}", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    public RestResponse transferBill(@PathVariable("id") Long billId, @RequestBody RestRequestObject<Account> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return billService.transferBill(requestObject, billId);
    }


    @RequestMapping(value = "bill/{id}", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a paginated list of all connection locations.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse bill(@ApiParam(value = "The ID of the existing consumer resource.", required = true) @PathVariable("id") Long accountId, @RequestBody RestRequestObject<BillRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return billService.bill(requestObject, accountId);
    }

    @RequestMapping(value = "/negativeReadings", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list of all negative readings.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse negativeReadings(@RequestBody RestRequestObject<AccountsReportRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return billService.getNegativeReadingsReport(requestObject);
    }

    @RequestMapping(value = "/meterStops", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list of all meter stops.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse meterStops(@RequestBody RestRequestObject<AccountsReportRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return billService.getMeterStopsReport(requestObject);
    }

    @RequestMapping(value = "/meterReadings", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list of all meter readings.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse meterReadings(@RequestBody RestRequestObject<AccountsReportRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return billService.getMeterReadingsReport(requestObject);
    }


    @RequestMapping(value = "/billedAmount", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list of all meter readings.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse billedAmount(@RequestBody RestRequestObject<AccountsReportRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return billService.getBilledAmountReport(requestObject);
    }

    @RequestMapping(value = "/checklist", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a list of all meter readings.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse billingChecklist(@RequestBody RestRequestObject<AccountsReportRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return billService.getBillingChecklistReport(requestObject);
    }

    @RequestMapping(value = "/billOnAverageUnits", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    public RestResponse getBillOnAverageUnits(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return billService.getBillOnAverageUnits();
    }

}
