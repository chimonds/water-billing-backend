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
package ke.co.suncha.simba.admin.security;

import java.util.*;

import com.auth0.jwt.internal.com.fasterxml.jackson.databind.JsonNode;
import com.auth0.jwt.internal.com.fasterxml.jackson.databind.ObjectMapper;
import com.auth0.jwt.internal.org.apache.commons.codec.binary.Base64;
import com.sun.org.apache.xpath.internal.operations.Bool;
import ke.co.suncha.simba.admin.models.SystemAction;
import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.models.UserAuth;
import ke.co.suncha.simba.admin.repositories.SystemActionRepository;
import ke.co.suncha.simba.admin.repositories.UserRepository;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.request.RestResponse;

import ke.co.suncha.simba.admin.service.CurrentUserService;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Service
public class AuthManager {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    SimbaOptionService optionService;

    @Autowired
    CurrentUserService currentUserService;

    @Autowired
    SystemActionRepository systemActionRepository;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public AuthManager() {

    }

    public RestResponse grant(String token, String action) {
        RestResponseObject obj = new RestResponseObject();
        RestResponse response;
        obj.setMessage("You are not authorized to perform action, please contact your admin.");
        response = new RestResponse(obj, HttpStatus.EXPECTATION_FAILED);

        Boolean grant = true;
        try {
            if (token != null && !"".equals(token)) {
                String[] pieces = token.split("\\.");
                if (pieces.length != 3) {
                    grant = false;
                }

                String emailAddress = "";
                if (grant) {
                    User user;
                    JsonNode jwtPayload = this.decodeAndParse(pieces[1]);
                    emailAddress = jwtPayload.get("email_address").asText();
                    user = userRepository.findByEmailAddress(emailAddress);

                    if (user == null) {
                        grant = false;
                    }

                    if (user.getUserRole() == null) {
                        grant = false;
                    }

                    if (grant) {
                        //get system action
                        SystemAction systemAction = systemActionRepository.findByName(action);
                        if (systemAction == null) {
                            systemAction = new SystemAction();
                            systemAction.setName(action);
                            systemAction.setDescription("Auto generated");
                            systemAction.setIsActive(true);
                            systemAction = systemActionRepository.save(systemAction);
                        }
                        if (!user.getUserRole().getSystemActions().contains(systemAction)) {
                            log.error(user.getEmailAddress() + " denied to perform:" + action.toUpperCase());

                            obj.setMessage("You are not authorized to perform action, please contact your admin.");
                            response = new RestResponse(obj, HttpStatus.EXPECTATION_FAILED);
                            return response;
                        }

                        //check if user needs to change password
                        if (action.compareToIgnoreCase("users_change_own_password") != 0) {
//                            log.info(user.getEmailAddress() + " change password status:" + user.getUserAuth().getResetAuth());
                            if (user.getUserAuth().getResetAuth() == true) {
                                log.error(user.getEmailAddress() + " denied to perform:" + action + ". User needs to change password.");
                                obj.setMessage("Access to this resource denied. Please change your password.");
                                response = new RestResponse(obj, HttpStatus.EXPECTATION_FAILED);
                                return response;
                            }
                        }

                        log.info(user.getEmailAddress() + " allowed to perform: " + action.toUpperCase());
                        obj.setMessage("Ok");
                        response = new RestResponse(obj, HttpStatus.OK);
                        return response;
                    }
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return response;
    }

    public Boolean passwordValid(User user, String password) {
        Boolean valid = false;
        // validate password hash
        BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();
        // passEncoder.
        if (passEncoder.matches(user.getEmailAddress().toLowerCase() + password, user.getUserAuth().getAuthPassword())) {
            if (!user.isActive()) {
                valid = false;
            } else {
                valid = true;
            }
        }
        return valid;
    }

    public RestResponse authenticate(Credential c) {
        try {
            User user = userRepository.findByEmailAddress(c.getUsername());
            if (user == null) {
                responseObject.setMessage("Invalid email/password");
                responseObject.setPayload("");
                response = new RestResponse(responseObject, HttpStatus.UNAUTHORIZED);
            } else {

                // validate password hash
                BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();
                // passEncoder.

                if (passEncoder.matches(user.getEmailAddress().toLowerCase() + c.getPassword(), user.getUserAuth().getAuthPassword())) {
                    if (!user.isActive()) {
                        log.error(user.getEmailAddress() + " account dissabled");
                        responseObject.setMessage("Your account is dissabled, please contact the administrator");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.UNAUTHORIZED);

                    } else {
                        // save last access
                        user.getUserAuth().setLastAccess(Calendar.getInstance());
                        userRepository.save(user);
                        currentUserService.setUser(user.getUserId());

                        // generate token
                        String token = this.generateToken(user);

                        LoginResponse loginResponse = new LoginResponse();
                        loginResponse.setToken(token);
                        loginResponse.setName(user.getFirstName() + " " + user.getLastName());

                        List<String> permissions = new ArrayList<>();
                        if (user.getUserRole() != null) {
                            if (!user.getUserRole().getSystemActions().isEmpty()) {
                                for (SystemAction systemAction : user.getUserRole().getSystemActions()) {
                                    permissions.add(systemAction.getName());
                                }
                            }
                        }
                        loginResponse.setPermissions(permissions);
                        log.info(user.getEmailAddress() + " Logged in successfully");
                        responseObject.setMessage("Logged in successfully");
                        responseObject.setPayload(loginResponse);
                        response = new RestResponse(responseObject, HttpStatus.OK);
                    }
                } else {
                    log.error(c.getUsername() + " Invalid username/password");
                    responseObject.setMessage("Invalid username/password");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.UNAUTHORIZED);
                }

            }
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
        }
        return response;
    }

    public static String encodePassword(String username, String password) {
        BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();
        // encode pass plus email address
        return passEncoder.encode(username.toLowerCase() + password);
    }

    private Integer getIdleSessionTimeout() {
        Integer timeout = 1;
        try {
            timeout = Integer.valueOf(optionService.getOption("SESSION_TIME_OUT:IDLE").getValue());
        } catch (Exception ex) {
        }
        return timeout;
    }

    private Integer getGlobalTokenTimeout() {
        Integer timeout = 1;
        try {
            timeout = Integer.valueOf(optionService.getOption("SESSION_TIME_OUT:GLOBAL").getValue());
        } catch (Exception ex) {
        }
        return timeout;
    }

    private String generateToken(User user) {
        String myToken = "";
        try {
            // Initialize
            //generate auth key for the session
            String authKey = UUID.randomUUID().toString();
            JWTSigner jwtSigner = new JWTSigner(authKey);

            Map<String, Object> map = new HashMap<String, Object>();

            map.put("email_address", user.getEmailAddress());

            // set options
            JWTSigner.Options options = new JWTSigner.Options();
            options.setJwtId(true);
            options.setExpirySeconds(this.getGlobalTokenTimeout());

            // get token
            myToken = jwtSigner.sign(map, options);

            //save user auth key
            user.getUserAuth().setAuthKey(authKey);
            userRepository.save(user);

        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
        }
        return myToken;
    }

    public JsonNode decodeAndParse(String b64String) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = new String(Base64.decodeBase64(b64String), "UTF-8");
        JsonNode jwtHeader = (JsonNode) mapper.readValue(jsonString, JsonNode.class);
        return jwtHeader;
    }


    public String getEmailFromToken(String token) {
        String emailAddress = "";
        try {
            if (token != null && !"".equals(token)) {
                String[] pieces = token.split("\\.");
                if (pieces.length != 3) {
                    return "";
                }
                JsonNode jwtPayload = this.decodeAndParse(pieces[1]);
                emailAddress = jwtPayload.get("email_address").asText();
            }
        } catch (Exception ex) {

        }
        return emailAddress;
    }

    public RestResponse tokenValid(String token) {
        RestResponseObject obj = new RestResponseObject();
        RestResponse response;

        //generate a random key
        String authKey = UUID.randomUUID().toString();

        try {
            if (token != null && !"".equals(token)) {
                String[] pieces = token.split("\\.");
                if (pieces.length != 3) {
                    obj.setMessage("Invalid token");
                    response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);
                    return response;
                }

                JsonNode jwtPayload = this.decodeAndParse(pieces[1]);

                String emailAddress = jwtPayload.get("email_address").asText();

                User user = userRepository.findByEmailAddress(emailAddress);
                authKey = user.getUserAuth().getAuthKey();

                Map<String, Object> decodedPayload = new JWTVerifier(authKey).verify(token);

                Calendar lastAccess = user.getUserAuth().getLastAccess();
                //add session idle timeout
                lastAccess.add(Calendar.SECOND, this.getIdleSessionTimeout());

                if (Calendar.getInstance().after(lastAccess)) {
                    obj.setMessage("Your session timed out");
                    log.info("Session timed out for:" + emailAddress);
                    response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);
                } else {
                    currentUserService.setUser(user.getUserId());
                    //good to go
                    obj.setMessage("Ok");
                    response = new RestResponse(obj, HttpStatus.OK);
                    // save last access
                    UserAuth auth = user.getUserAuth();
                    auth.setLastAccess(Calendar.getInstance());
                    user.setUserAuth(auth);
                    userRepository.save(user);
                }

            } else {
                obj.setMessage("Invalid signature");
                response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);
            }
        } catch (SignatureException signatureException) {
            log.error(signatureException.getLocalizedMessage());
            obj.setMessage("Invalid signature");
            response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);
        } catch (IllegalStateException illegalStateException) {
            obj.setMessage("Invalid Token!");
            response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);
            log.error(illegalStateException.getLocalizedMessage());
        } catch (IOException e) {
            obj.setMessage("Invalid token");
            response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);
            log.error(e.getLocalizedMessage());
        } catch (InvalidKeyException e) {
            obj.setMessage("Invalid token");
            response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);
            log.error(e.getLocalizedMessage());
        } catch (JWTVerifyException e) {
            obj.setMessage("Invalid token");
            response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);
            log.error(e.getLocalizedMessage());

        } catch (NoSuchAlgorithmException e) {
            obj.setMessage("Invalid token");
            response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);
            log.error(e.getLocalizedMessage());
        }
        return response;
    }
}
