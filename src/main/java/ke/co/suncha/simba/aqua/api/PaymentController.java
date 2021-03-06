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
package ke.co.suncha.simba.aqua.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.models.Payment;
import ke.co.suncha.simba.aqua.services.PaymentService;

import ke.co.suncha.simba.aqua.utils.BillRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@RestController
@RequestMapping(value = "/api/v1/payments")
@Api(value = "Connection payments", description = "Connection payment API")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;


    @RequestMapping(value = "/void", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Void a receipt resource.", notes = "Returns the URL of the new resource in the Location header.")
    public RestResponse voidReceipt(@RequestBody RestRequestObject<Payment> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return this.paymentService.voidReceipt(requestObject);
    }

    @RequestMapping(value = "/create/{id}", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a payment resource.", notes = "Returns the URL of the new resource in the Location header.")
    public RestResponse createByAccount(@ApiParam(value = "The ID of the existing account resource.", required = true) @PathVariable("id") Long id, @RequestBody RestRequestObject<Payment> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return this.paymentService.createByAccount(requestObject, id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a paginated list of all account bills.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse getAll(@ApiParam(value = "The ID of the existing account resource.", required = true) @PathVariable("id") Long account_id, @RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return paymentService.getAllByAccount(requestObject, account_id);
    }


    @RequestMapping(value = "transfer/{id}", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a paginated list of all connection locations.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse transferPayment(@ApiParam(value = "The ID of the existing consumer resource.", required = true) @PathVariable("id") Long accountId, @RequestBody RestRequestObject<Payment> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return paymentService.transferPayment(requestObject, accountId);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a paginated list of all payments.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse getAllByReceiptNo(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return paymentService.getAllByReceiptNo(requestObject);
    }
}
