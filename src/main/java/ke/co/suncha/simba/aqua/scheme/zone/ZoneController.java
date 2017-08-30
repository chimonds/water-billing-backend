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
package ke.co.suncha.simba.aqua.scheme.zone;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.scheme.Scheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@RestController
@RequestMapping(value = "/api/v1/zones")
@Api(value = "Connection zones", description = "Connection zone API")
public class ZoneController extends AbstractRestHandler {
    @Autowired
    private ZoneService zoneService;

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a zone resource.", notes = "Returns the URL of the new resource in the Location header.")
    public RestResponse createUser(@RequestBody RestRequestObject<Zone> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return this.zoneService.create(requestObject);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a paginated list of all connection locations.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse getAll(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return zoneService.getAllByFilter(requestObject);
    }

    @RequestMapping(value = "/byScheme", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ApiOperation(value = "Get a paginated list of all connection locations.", notes = "The list is paginated. You can provide a page number (default 0) and a page size (default 100)")
    public RestResponse getAllByScheme(@RequestBody RestRequestObject<Scheme> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return zoneService.getAllByScheme(requestObject);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Update a billing month resource.", notes = "You have to provide a valid user role ID in the URL and in the payload. The ID attribute can not be updated.")
    public RestResponse update(@ApiParam(value = "The ID of the existing zone resource.", required = true) @PathVariable("id") Long id, @RequestBody RestRequestObject<Zone> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return zoneService.update(requestObject);
    }


    @RequestMapping(value = "one/{id}", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public RestResponse findOne(@ApiParam(value = "The ID of the existing user role resource.", required = true) @PathVariable("id") Long id, @RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return zoneService.getById(requestObject, id);
    }

}