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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.models.UserAuth;
import ke.co.suncha.simba.admin.repositories.UserRepository;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.request.RestResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 *
 */
@Service
public class AuthManager {

	@Autowired
	private UserRepository userRepository;

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	protected final String AUTH_SECRET_CODE = "XXXXXXXX";
	// TODO; change this
	protected final Long SESSION_TIME_OUT = 300L;
	private RestResponse response;
	private RestResponseObject responseObject = new RestResponseObject();

	public AuthManager() {

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
						responseObject.setMessage("Your account is dissabled, please contact the administrator");
						responseObject.setPayload("");
						response = new RestResponse(responseObject, HttpStatus.UNAUTHORIZED);

					} else {
						// save last access
						user.getUserAuth().setLastAccess(Calendar.getInstance());
						userRepository.save(user);

						// generate token
						String token = this.generateToken(user);
						LoginResponse lr = new LoginResponse();
						lr.setToken(token);
						lr.setName(user.getFirstName() + " " + user.getLastName());

						responseObject.setMessage("Logged in successfully");
						responseObject.setPayload(lr);
						response = new RestResponse(responseObject, HttpStatus.OK);
					}
				} else {
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

	private String generateToken(User user) {
		String myToken = "";
		try {
			// Initialize
			JWTSigner jwtSigner = new JWTSigner(AUTH_SECRET_CODE);

			Map<String, Object> map = new HashMap<String, Object>();

			map.put("email_address", user.getEmailAddress());
			map.put("user_id", user.getUserId());

			// set options
			JWTSigner.Options options = new JWTSigner.Options();
			options.setJwtId(true);

			// get token
			myToken = jwtSigner.sign(map, options);
		} catch (Exception ex) {
			log.error(ex.getLocalizedMessage());
		}
		return myToken;
	}

	public RestResponse tokenValid(String token) {
		RestResponseObject obj = new RestResponseObject();
		RestResponse response;

		try {
			Map<String, Object> decodedPayload = new JWTVerifier(AUTH_SECRET_CODE).verify(token);

			// get last access time
			String emailAddress = decodedPayload.get("email_address").toString();
			User user = userRepository.findByEmailAddress(emailAddress);

			Calendar lastAccessDate = user.getUserAuth().getLastAccess();

			Calendar today = Calendar.getInstance();

			// difference in miliseconds
			Long diffInMilliseconds = today.getTimeInMillis() - lastAccessDate.getTimeInMillis();

			System.out.println("diffInMilliseconds:" + diffInMilliseconds);

			long seconds = new Long(Long.MAX_VALUE);

			// TODO; i do not know y this happens f***
			diffInMilliseconds = Math.abs(diffInMilliseconds);
			if (diffInMilliseconds > 0) {
				seconds = diffInMilliseconds / 1000;
			}

			System.out.println("IDLE in seconds:" + seconds);

			if (seconds > SESSION_TIME_OUT) {
				obj.setMessage("Your session timed out");
				response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);

			} else {
				obj.setMessage("Ok");
				response = new RestResponse(obj, HttpStatus.OK);
				// save last access
				UserAuth auth = user.getUserAuth();
				auth.setLastAccess(today);
				user.setUserAuth(auth);
				userRepository.save(user);
			}

		} catch (SignatureException signatureException) {
			log.error(signatureException.getLocalizedMessage());
			obj.setMessage("Invalid signature");
			response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);
		} catch (IllegalStateException illegalStateException) {
			obj.setMessage("Invalid Token!");
			response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);

			System.err.println("Invalid Token! " + illegalStateException);
			log.error(illegalStateException.getLocalizedMessage());
		} catch (IOException e) {
			obj.setMessage("Invalid token");
			response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);

			log.error(e.getLocalizedMessage());
		} catch (InvalidKeyException e) {
			obj.setMessage("Invalid token");
			response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);

			System.err.println(e.getMessage());
			log.error(e.getLocalizedMessage());
		} catch (JWTVerifyException e) {
			obj.setMessage("Invalid token");
			response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);

			System.err.println(e.getMessage());
			log.error(e.getLocalizedMessage());

		} catch (NoSuchAlgorithmException e) {
			obj.setMessage("Invalid token");
			response = new RestResponse(obj, HttpStatus.UNAUTHORIZED);
			System.err.println(e.getMessage());
			log.error(e.getLocalizedMessage());
		}
		return response;
	}
}
