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
package ke.co.suncha.simba.admin.service;


import java.util.List;

import ke.co.suncha.simba.admin.helpers.RolePermission;
import ke.co.suncha.simba.admin.models.SystemAction;
import ke.co.suncha.simba.admin.models.UserRole;
import ke.co.suncha.simba.admin.repositories.SystemActionRepository;
import ke.co.suncha.simba.admin.repositories.UserRoleRepository;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.security.AuthManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
 */
@Service
public class SystemActionService {
	@Autowired
	private SystemActionRepository systemActionRepository;

	@Autowired
	private AuthManager authManager;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	CounterService counterService;

	@Autowired
	GaugeService gaugeService;

	private RestResponse response;
	private RestResponseObject responseBody;

	public SystemActionService() {

	}

	public void create(String action){
		if(systemActionRepository.findByName(action)==null){
			SystemAction systemAction= new SystemAction();
			systemAction.setName(action);
			systemAction.setDescription("Auto generated");
			systemActionRepository.save(systemAction);
		}
	}

	public RestResponse create(SystemAction systemAction) {
		SystemAction sa = systemActionRepository.findByName(systemAction.getName());

		if (sa != null) {
			responseBody = new RestResponseObject("Permission already exists", "");
			response = new RestResponse(responseBody, HttpStatus.NOT_FOUND);
		} else {
			try {
				SystemAction createdSystemAction = systemActionRepository.save(systemAction);

				responseBody = new RestResponseObject("Permission created successfully", createdSystemAction);
				response = new RestResponse(responseBody, HttpStatus.CREATED);

			} catch (Exception ex) {
				responseBody = new RestResponseObject(ex.getLocalizedMessage(), "");

				response = new RestResponse(responseBody, HttpStatus.EXPECTATION_FAILED);
			}
		}
		return response;
	}

	public RestResponse getAllUserRoleSystemActions(RestRequestObject<String> requestObject, Long id) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
				List<SystemAction> available;

				UserRole ur = userRoleRepository.findOne(id);
				if (ur == null) {
					responseBody = new RestResponseObject("Invalid user role", "");
					response = new RestResponse(responseBody, HttpStatus.EXPECTATION_FAILED);
				} else {
					// get available system actions here
					available = systemActionRepository.findAll();

					if (available.isEmpty()) {
						responseBody = new RestResponseObject("Your search did not match any records", "");
						response = new RestResponse(responseBody, HttpStatus.EXPECTATION_FAILED);
					} else {

						//
						List<SystemAction> assigned = ur.getSystemActions();
						available.removeAll(assigned);

						RolePermission rolePermissions = new RolePermission();
						rolePermissions.setAssigned(assigned);
						rolePermissions.setAvailable(available);

						// set response
						responseBody = new RestResponseObject("Fetched data successfully", rolePermissions);
						response = new RestResponse(responseBody, HttpStatus.OK);
					}
				}
			}

		} catch (Exception ex) {
			responseBody = new RestResponseObject(ex.getLocalizedMessage(), "");
			response = new RestResponse(responseBody, HttpStatus.EXPECTATION_FAILED);

		}
		return response;
	}
}
