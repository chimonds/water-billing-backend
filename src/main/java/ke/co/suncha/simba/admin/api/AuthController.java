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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.security.Credential;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
 */
@RestController
@RequestMapping(value = "/api/v1/auth")
@Api(value = "auth", description = "Authentication API")
public class AuthController extends AbstractRestHandler {
	@Autowired
	private AuthManager authService;

	@RequestMapping(value = "", method = RequestMethod.POST, consumes = { "application/json", "application/xml" }, produces = { "application/json", "application/xml" })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Authenticate a user resource.", notes = "Returns the URL of the new resource in the Location header.")
	public RestResponse authenticate(@RequestBody Credential credential, HttpServletRequest request, HttpServletResponse response) {
		return this.authService.authenticate(credential);
	}
}