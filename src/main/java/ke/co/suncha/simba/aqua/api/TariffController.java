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

import com.wordnik.swagger.annotations.ApiParam;
import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.services.TariffService;

import ke.co.suncha.simba.aqua.utils.BillMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
 */
@RestController
@RequestMapping(value = "/api/v1/tariffs")
@Api(value = "Connection tariffs", description = "Connection tariff API")
public class TariffController extends AbstractRestHandler {
	@Autowired
	private TariffService service;

	@RequestMapping(value = "", method = RequestMethod.POST, consumes = { "application/json", "application/xml" }, produces = { "application/json", "application/xml" })
	@ApiOperation(value = "Get a paginated list of all account tariffs.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
	public RestResponse getAll(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
		return service.getAllByFilter(requestObject);
	}

	@RequestMapping(value = "calculate/{id}", method = RequestMethod.POST, consumes = { "application/json", "application/xml" }, produces = { "application/json", "application/xml" })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value = "Calculate  resource.", notes = "")
	public RestResponse update(@ApiParam(value = "The ID of the existing account resource", required = true) @PathVariable("id") Long accountId, @RequestBody RestRequestObject<BillMeta> requestObject, HttpServletRequest request, HttpServletResponse response) {
		return service.calculate(requestObject,accountId);
	}
}
