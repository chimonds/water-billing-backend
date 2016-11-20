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
import edu.vt.middleware.password.*;
import javassist.tools.framedump;
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

import ke.co.suncha.simba.admin.security.Credential;
import ke.co.suncha.simba.admin.security.PasswordReset;
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

import java.util.ArrayList;
import java.util.List;

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

    public User getByEmailAddress(String emailAddress) {
        return userRepository.findByEmailAddress(emailAddress);
    }

    public RestResponse create(RestRequestObject<User> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "users_create");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
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
                    String pass = passEncoder.encode(user.getEmailAddress().toLowerCase() + "123456");

                    auth.setAuthPassword(pass);
                    auth.setResetAuth(true);
                    user.setUserAuth(auth);


                    // send password

                    // create resource
                    User createdUser = userRepository.save(user);

                    // package response
                    responseObject.setMessage("User created successfully. The default user password is 123456");
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

    public RestResponse restPassword(RestRequestObject<User> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "users_resetPassword");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                User user = requestObject.getObject();
                User u = userRepository.findByEmailAddress(user.getEmailAddress());
                if (u == null) {
                    responseObject.setMessage("User does not exist");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else {

                    // generate and set password;
                    UserAuth auth = new UserAuth();
                    BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();
                    // encode pass plus email address
                    String pass = passEncoder.encode(user.getEmailAddress().toLowerCase() + "123456");

                    auth.setAuthPassword(pass);
                    auth.setResetAuth(true);
                    u.setUserAuth(auth);


                    // send password

                    // create resource
                    User createdUser = userRepository.save(u);

                    // package response
                    responseObject.setMessage("User password reset  to 123456.");
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

    public RestResponse updatePassword(RestRequestObject<PasswordReset> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "users_change_own_password");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                PasswordReset passwordReset = requestObject.getObject();
                User user = userRepository.findByEmailAddress(this.authManager.getEmailFromToken(requestObject.getToken()));

                //authenticate user

                if (!authManager.passwordValid(user, passwordReset.getExistingPassword())) {
                    responseObject.setMessage("Your current password does not match.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (passwordReset.getNewPassword().compareTo(passwordReset.getConfirmPassword()) != 0) {
                    responseObject.setMessage("Your passwords do not match.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (passwordReset.getNewPassword().compareTo(passwordReset.getExistingPassword()) == 0) {
                    responseObject.setMessage("Your new password can not be the same as the existing one.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                //password must be between 8 and 16 chars long
                LengthRule lengthRule = new LengthRule(6, 16);

                // don't allow whitespace
                WhitespaceRule whitespaceRule = new WhitespaceRule();

                // control allowed characters
                CharacterCharacteristicsRule charRule = new CharacterCharacteristicsRule();

                // require at least 1 digit in passwords
                charRule.getRules().add(new DigitCharacterRule(1));

                // require at least 1 non-alphanumeric char
                charRule.getRules().add(new NonAlphanumericCharacterRule(1));

                // require at least 1 upper case char
                charRule.getRules().add(new UppercaseCharacterRule(1));

                // require at least 1 lower case char
                charRule.getRules().add(new LowercaseCharacterRule(1));

                // require at least 3 of the previous rules be met
                charRule.setNumberOfCharacteristics(3);

                // don't allow alphabetical sequences
                AlphabeticalSequenceRule alphaSeqRule = new AlphabeticalSequenceRule();

                // don't allow numerical sequences of length 3
                //NumericalSequenceRule numSeqRule = new NumericalSequenceRule(3);

                // don't allow qwerty sequences
                QwertySequenceRule qwertySeqRule = new QwertySequenceRule();

                // don't allow 4 repeat characters
                RepeatCharacterRegexRule repeatRule = new RepeatCharacterRegexRule(4);

                // group all rules together in a List
                List<Rule> ruleList = new ArrayList<Rule>();
                ruleList.add(lengthRule);
                ruleList.add(whitespaceRule);
                ruleList.add(charRule);
                ruleList.add(alphaSeqRule);
                //ruleList.add(numSeqRule);
                ruleList.add(qwertySeqRule);
                ruleList.add(repeatRule);

                PasswordValidator validator = new PasswordValidator(ruleList);
                PasswordData passwordData = new PasswordData(new Password(passwordReset.getNewPassword()));

                RuleResult result = validator.validate(passwordData);
                if (!result.isValid()) {
                    String errorMsg = "";
                    for (String msg : validator.getMessages(result)) {
                        errorMsg += msg + "\n";
                    }

                    responseObject.setMessage("Invalid password.\n" + errorMsg);
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                UserAuth auth = user.getUserAuth();
                auth.setAuthPassword(AuthManager.encodePassword(user.getEmailAddress().toLowerCase(), passwordReset.getNewPassword()));
                auth.setResetAuth(false);
                user.setUserAuth(auth);
                userRepository.save(user);

                responseObject.setMessage("Your password has been changed successfully. Please logout and login again using your new password");
                responseObject.setPayload("");
                response = new RestResponse(responseObject, HttpStatus.OK);
            }

        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);

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
                response = authManager.grant(requestObject.getToken(), "users_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
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
                response = authManager.grant(requestObject.getToken(), "users_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                RestPageRequest p = requestObject.getObject();

                Page<User> pageOfUsers;
                if (p.getFilter().isEmpty()) {
                    log.info("Getting user list..");
                    pageOfUsers = userRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                    log.info("users found:" + pageOfUsers.getTotalElements());
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
            ex.printStackTrace();
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }
}
