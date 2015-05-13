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
import ke.co.suncha.simba.aqua.models.BillingMonth;
import ke.co.suncha.simba.aqua.repository.BillingMonthRepository;

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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
 */
@Service
public class BillingMonthService {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BillingMonthRepository billingMonthRepository;

	@Autowired
	private AuthManager authManager;

	@Autowired
	CounterService counterService;

	@Autowired
	GaugeService gaugeService;

	private RestResponse response;
	private RestResponseObject responseObject = new RestResponseObject();

	public BillingMonthService() {

	}

	@Transactional
	public RestResponse update(RestRequestObject<BillingMonth> requestObject) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "billing_month_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

				BillingMonth billingMonth = requestObject.getObject();
				BillingMonth bm = billingMonthRepository.findOne(billingMonth.getBillingMonthId());
				if (bm == null) {
					responseObject.setMessage("Billing month not found");
					response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
				} else {

					if (billingMonth.getCurrent() == 1) {
						if (billingMonthRepository.countWithCurrent(1) > 0) {
							responseObject.setMessage("Please close all billing dates before opening a new billing date");
							response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
						} else {
							// Open month
							bm.setCurrent(billingMonth.getCurrent());
							// save
							billingMonthRepository.save(bm);
							responseObject.setMessage("Billing month updated successfully");
							responseObject.setPayload(bm);
							response = new RestResponse(responseObject, HttpStatus.OK);
						}
					} else {
						// close month
						bm.setCurrent(billingMonth.getCurrent());
						// save
						billingMonthRepository.save(bm);
						responseObject.setMessage("Billing month updated successfully");
						responseObject.setPayload(bm);
						response = new RestResponse(responseObject, HttpStatus.OK);
					}
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
		return new Sort(Sort.Direction.DESC, "month");
	}

	public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
				response = authManager.grant(requestObject.getToken(), "billing_month_view");
				if (response.getStatusCode() != HttpStatus.OK) {
					return response;
				}

				RestPageRequest p = requestObject.getObject();

				Page<BillingMonth> pageOfObjects;

				pageOfObjects = billingMonthRepository.findByIsEnabled(1, new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));

				if (pageOfObjects.hasContent()) {

					responseObject.setMessage("Fetched data successfully");
					responseObject.setPayload(pageOfObjects);
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

	public RestResponse getActiveBillingMonth(RestRequestObject<RestPageRequest> requestObject) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
				response = authManager.grant(requestObject.getToken(), "billing_month_get_active");
				if (response.getStatusCode() != HttpStatus.OK) {
					return response;
				}

				BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);
				if (billingMonth != null) {
					responseObject.setMessage("Fetched data successfully");
					responseObject.setPayload(billingMonth);
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

	public RestResponse getAll(RestRequestObject<RestPageRequest> requestObject) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
				response = authManager.grant(requestObject.getToken(), "billing_month_list");
				if (response.getStatusCode() != HttpStatus.OK) {
					return response;
				}

				RestPageRequest p = requestObject.getObject();

				List<BillingMonth> billingMonths;

				billingMonths = billingMonthRepository.findAllByIsEnabledOrderByMonthDesc(1);
				if (!billingMonths.isEmpty()) {

					responseObject.setMessage("Fetched data successfully");
					responseObject.setPayload(billingMonths);
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
