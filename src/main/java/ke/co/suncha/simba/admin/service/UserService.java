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

import com.auth0.jwt.internal.com.fasterxml.jackson.databind.JsonNode;
import ke.co.suncha.simba.admin.models.SystemAction;
import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.models.UserAuth;
import ke.co.suncha.simba.admin.repositories.SystemActionRepository;
import ke.co.suncha.simba.admin.repositories.UserRepository;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.security.AuthManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Service
public class UserService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    private SystemActionRepository systemActionRepository;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public UserService() {

    }



    public RestResponse create(RestRequestObject<User> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {

                User user = requestObject.getObject();
                User u = userRepository.findByEmailAddress(user.getEmailAddress());
                if (u != null) {
                    responseObject.setMessage("User already exists");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);

                } else {

                    // generate and set password;
                    UserAuth auth = new UserAuth();

                    BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();

                    // encode pass plus email address
                    String pass = passEncoder.encode(user.getEmailAddress().toLowerCase() + "simbaone");

                    auth.setAuthPassword(pass);

                    user.setUserAuth(auth);

                    // send password

                    // create resource
                    User createdUser = userRepository.save(user);

                    // package response
                    responseObject.setMessage("User created successfully. Log in instructions have been sent to " + user.getEmailAddress());
                    responseObject.setPayload(createdUser);
                    response = new RestResponse(responseObject, HttpStatus.CREATED);
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);

            //
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getUser(long id) {
        try {
            User user = userRepository.findOne(id);
            if (user == null) {
                responseObject.setMessage("User  not found.");
                response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
            } else {
                responseObject.setMessage("Data found");
                responseObject.setPayload(user);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse update(RestRequestObject<User> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                User user = requestObject.getObject();
                User u = userRepository.findOne(user.getUserId());
                if (u == null) {
                    responseObject.setMessage("User not found");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else {
                    // setup resource
                    u.setFirstName(user.getFirstName());
                    u.setLastName(user.getLastName());
                    u.setActive(user.isActive());

                    //
                    u.setUserRole(user.getUserRole());

                    // save
                    userRepository.save(u);
                    responseObject.setMessage("User  updated successfully");
                    responseObject.setPayload(u);
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

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {

                RestPageRequest p = requestObject.getObject();

                Page<User> pageOfUsers;
                if (p.getFilter().isEmpty()) {
                    pageOfUsers = userRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                } else {
                    pageOfUsers = userRepository.findByEmailAddressContaining(p.getFilter(), new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));

                }
                if (pageOfUsers.hasContent()) {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(pageOfUsers);
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
