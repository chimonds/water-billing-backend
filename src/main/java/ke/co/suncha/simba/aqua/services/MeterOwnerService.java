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
package ke.co.suncha.simba.aqua.services;

import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.aqua.models.MeterOwner;
import ke.co.suncha.simba.aqua.repository.MeterOwnerRepository;

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
public class MeterOwnerService {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private MeterOwnerRepository meterOwnerRepository;

	@Autowired
	private AuthManager authManager;

	@Autowired
	CounterService counterService;

	@Autowired
	GaugeService gaugeService;

	private RestResponse response;
	private RestResponseObject responseObject = new RestResponseObject();

	public MeterOwnerService() {

	}

	private Sort sortByDateAddedDesc() {
		return new Sort(Sort.Direction.DESC, "createdOn");
	}

	public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
				response = authManager.grant(requestObject.getToken(), "meter_view");
				if (response.getStatusCode() != HttpStatus.OK) {
					return response;
				}

				RestPageRequest p = requestObject.getObject();

				Page<MeterOwner> page;
				page = meterOwnerRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));

				if (page.hasContent()) {

					responseObject.setMessage("Fetched data successfully");
					responseObject.setPayload(page);
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
