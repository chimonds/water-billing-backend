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

import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.models.Meter;
import ke.co.suncha.simba.aqua.services.MeterService;

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
 *
 */
@RestController
@RequestMapping(value = "/api/v1/meters")
@Api(value = "Meters", description = "Connection meters API")
public class MeterController extends AbstractRestHandler {
	@Autowired
	private MeterService meterService;

	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = { "application/json", "application/xml" }, produces = { "application/json", "application/xml" })
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation(value = "Create a meter resource.", notes = "Returns the URL of the new resource in the Location header.")
	public RestResponse create(@RequestBody RestRequestObject<Meter> requestObject, HttpServletRequest request, HttpServletResponse response) {
		return this.meterService.create(requestObject);
	}

	@RequestMapping(value = "", method = RequestMethod.POST, consumes = { "application/json", "application/xml" }, produces = { "application/json", "application/xml" })
	@ApiOperation(value = "Get a paginated list of all connection meters.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
	public RestResponse getAll(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
		return meterService.getAllByFilter(requestObject);
	}

	@RequestMapping(value = "", method = RequestMethod.PUT, consumes = { "application/json", "application/xml" }, produces = { "application/json", "application/xml" })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value = "Update a billing month resource.", notes = "You have to provide a valid user role ID in the URL and in the payload. The ID attribute can not be updated.")
	public RestResponse update(@ApiParam(value = "The ID of the existing zone resource.", required = true) @PathVariable("id") Long id, @RequestBody RestRequestObject<Meter> requestObject, HttpServletRequest request, HttpServletResponse response) {
		return meterService.update(requestObject);
	}

	@RequestMapping(value = "allocate/{id}", method = RequestMethod.PUT, consumes = { "application/json", "application/xml" }, produces = { "application/json", "application/xml" })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value = "Update a billing month resource.", notes = "You have to provide a valid user role ID in the URL and in the payload. The ID attribute can not be updated.")
	public RestResponse allocation(@ApiParam(value = "The ID of the existing zone resource.", required = true) @PathVariable("id") Long id, @RequestBody RestRequestObject<Meter> requestObject, HttpServletRequest request, HttpServletResponse response) {
		return meterService.allocate(requestObject, id);
	}
	
	@RequestMapping(value = "deallocate/{id}", method = RequestMethod.PUT, consumes = { "application/json", "application/xml" }, produces = { "application/json", "application/xml" })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value = "Update a billing month resource.", notes = "You have to provide a valid user role ID in the URL and in the payload. The ID attribute can not be updated.")
	public RestResponse deallocate(@ApiParam(value = "The ID of the existing zone resource.", required = true) @PathVariable("id") Long id, @RequestBody RestRequestObject<Meter> requestObject, HttpServletRequest request, HttpServletResponse response) {
		return meterService.deallocate(requestObject, id);
	}
}
