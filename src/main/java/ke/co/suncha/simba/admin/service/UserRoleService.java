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

import java.util.ArrayList;
import java.util.List;

import ke.co.suncha.simba.admin.models.SystemAction;
import ke.co.suncha.simba.admin.models.UserRole;
import ke.co.suncha.simba.admin.repositories.UserRoleRepository;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.utils.CustomPage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
 */
@Service
public class UserRoleService {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private AuthManager authManager;

	@Autowired
	CounterService counterService;

	@Autowired
	GaugeService gaugeService;

	private RestResponse response;
	private RestResponseObject responseObject = new RestResponseObject();

	public UserRoleService() {
	}

	public RestResponse create(RestRequestObject<UserRole> requestObject) {

		response = authManager.tokenValid(requestObject.getToken());

		if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
			UserRole userRole = requestObject.getObject();
			UserRole ur = userRoleRepository.findByName(userRole.getName());

			if (ur != null) {
				responseObject.setMessage("User role already exists");
				response = new RestResponse(responseObject, HttpStatus.CONFLICT);

			} else {
				try {
					// create resource
					UserRole createdUserRole = userRoleRepository.save(userRole);

					// package response
					responseObject.setMessage("User role created successfully");
					responseObject.setPayload(createdUserRole);
					response = new RestResponse(responseObject, HttpStatus.CREATED);
				} catch (Exception ex) {
					responseObject.setMessage(ex.getLocalizedMessage());
					response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);

					//
					log.error(ex.getLocalizedMessage());
				}
			}
		}
		return response;
	}

	public RestResponse getUserRole(long id) {
		try {
			UserRole ur = userRoleRepository.findOne(id);
			if (ur == null) {
				responseObject.setMessage("User role not found.");

				response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
			} else {
				responseObject.setMessage("Data found");
				responseObject.setPayload(ur);
				response = new RestResponse(responseObject, HttpStatus.OK);
			}

		} catch (Exception ex) {
			responseObject.setMessage(ex.getLocalizedMessage());
			response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
			log.error(ex.getLocalizedMessage());
		}
		return response;
	}

	public RestResponse updateWithPermissions(RestRequestObject<List<SystemAction>> requestObject, Long userRoleId) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {

				List<SystemAction> systemActions = requestObject.getObject();

				UserRole ur = userRoleRepository.findOne(userRoleId);
				if (ur == null) {
					responseObject.setMessage("User role not found");
					response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
				} else {
					// update system actions
					ur.setSystemActions(systemActions);

					// save
					userRoleRepository.save(ur);

					responseObject.setMessage("User role permissions updated successfully");
					responseObject.setPayload(ur);
					response = new RestResponse(responseObject, HttpStatus.OK);
				}
			}
		} catch (Exception ex) {
			responseObject.setMessage(ex.getLocalizedMessage());
			response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);

			log.error(ex.getLocalizedMessage());
		}
		return response;
	}

	public RestResponse update(RestRequestObject<UserRole> requestObject, Long userRoleId) {

		response = authManager.tokenValid(requestObject.getToken());
		try {
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
				UserRole userRole = requestObject.getObject();
				UserRole ur = userRoleRepository.findOne(userRoleId);
				if (ur == null) {
					responseObject.setMessage("User role not found");
					response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
				} else {
					// setup resource
					ur.setDescription(userRole.getDescription());
					ur.setName(userRole.getName());

					// save
					userRoleRepository.save(ur);

					responseObject.setMessage("User role updated successfully");
					responseObject.setPayload(ur);
					response = new RestResponse(responseObject, HttpStatus.OK);
				}
			}
		} catch (Exception ex) {
			responseObject.setMessage(ex.getLocalizedMessage());
			response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);

			log.error(ex.getLocalizedMessage());
		}
		return response;
	}

	public void delete(Long id) {
		userRoleRepository.delete(id);
	}

	private Sort sortByDateAddedDesc() {
		return new Sort(Sort.Direction.DESC, "createdOn");
	}

	public RestResponse getAllUserRolesByName(RestRequestObject<RestPageRequest> requestObject) {
		try {
			response = authManager.tokenValid(requestObject.getToken());

			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
				RestPageRequest pageRequest = requestObject.getObject();
				Page<UserRole> pageOfUserRoles;
				if (pageRequest.getFilter().isEmpty()) {
					pageOfUserRoles = userRoleRepository.findAll(new PageRequest(pageRequest.getPage(), pageRequest.getSize(), sortByDateAddedDesc()));
				} else {
					pageOfUserRoles = userRoleRepository.findByNameContaining(pageRequest.getFilter(), new PageRequest(pageRequest.getPage(), pageRequest.getSize(), sortByDateAddedDesc()));
				}
				if (pageOfUserRoles.hasContent()) {

					// null sensitive data and get the role
					List<UserRole> urList = pageOfUserRoles.getContent();
					List<UserRole> newUserRoles = new ArrayList<>();
					for (UserRole ur : urList) {
						ur.setSystemActions(null);
						newUserRoles.add(ur);
					}

					CustomPage cPage = new CustomPage();
					cPage.setContent(newUserRoles);
					cPage.setFirst(pageOfUserRoles.isFirst());
					cPage.setLast(pageOfUserRoles.isLast());

					cPage.setNumberOfElements(pageOfUserRoles.getNumberOfElements());
					cPage.setTotalElements(pageOfUserRoles.getTotalElements());
					cPage.setTotalPages(pageOfUserRoles.getTotalPages());

					responseObject.setMessage("Fetched data successfully");
					responseObject.setPayload(cPage);
					response = new RestResponse(responseObject, HttpStatus.OK);

				} else {
					responseObject.setMessage("Your search did not match any records");
					response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
				}

			}
		} catch (Exception ex) {
			responseObject.setMessage(ex.getLocalizedMessage());
			response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
			log.error(ex.getLocalizedMessage());
		}
		return response;
	}
}
