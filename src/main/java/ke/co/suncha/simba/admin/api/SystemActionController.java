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
package ke.co.suncha.simba.admin.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ke.co.suncha.simba.admin.models.SystemAction;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.service.SystemActionService;
import ke.co.suncha.simba.admin.service.UserRoleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
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
@RequestMapping(value = "/api/v1/permissions")
@Api(value = "permissions", description = "User role permissions API")
public class SystemActionController extends AbstractRestHandler {
	
	@Autowired
	private SystemActionService systemActionService;

	@Autowired
	private UserRoleService userRoleService;

	@RequestMapping(value = "/{id}", method = RequestMethod.POST, produces = {
			"application/json", "application/xml" })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Get a list of all system permissions.", notes = "You have to provide a valid user role id")
	public @ResponseBody RestResponse getUserRole(
			@ApiParam(value = "The ID of the user role", required = true) @PathVariable("id") Long id,
			@RequestBody RestRequestObject<String> requestObject, 
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return this.systemActionService.getAllUserRoleSystemActions(requestObject,id);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = {
			"application/json", "application/xml" }, produces = {
			"application/json", "application/xml" })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value = "Update a user role resource with permissions.", notes = "You have to provide a valid user role ID in the URL and in the payload. The ID attribute can not be updated.")
	public RestResponse updateUserRolePermissions(
			@ApiParam(value = "The ID of the existing user role resource.", required = true) @PathVariable("id") Long id,
			//@RequestBody List<SystemAction> systemActions,
			@RequestBody RestRequestObject<List<SystemAction>> requestObject, 
			HttpServletRequest request, HttpServletResponse response) {
		return this.userRoleService.updateWithPermissions(requestObject, id);
	}
}
