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

import java.util.List;

import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.aqua.models.Account;
import ke.co.suncha.simba.aqua.models.Consumer;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.repository.ConsumerRepository;

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
public class AccountService {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private ConsumerRepository consumerRepository;

	@Autowired
	private AuthManager authManager;

	@Autowired
	CounterService counterService;

	@Autowired
	GaugeService gaugeService;

	private RestResponse response;
	private RestResponseObject responseObject = new RestResponseObject();

	public AccountService() {

	}

	public RestResponse create(RestRequestObject<Account> requestObject, Long id) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {

				Account account = requestObject.getObject();
				Account acc = accountRepository.findByaccNo(account.getAccNo());

				Consumer consumer = consumerRepository.findOne(id);

				if (acc != null) {
					responseObject.setMessage("Account already exists");
					response = new RestResponse(responseObject, HttpStatus.CONFLICT);
				} else if (consumer == null) {
					responseObject.setMessage("Invalid consumer");

					response = new RestResponse(responseObject, HttpStatus.CONFLICT);
				} else {
					// create resource
					acc = new Account();

					acc.setAccNo(account.getAccNo());
					acc.setAverageConsumption(account.getAverageConsumption());
					acc.setBalanceBroughtForward(account.getBalanceBroughtForward());
					Account created = accountRepository.save(acc);

					created.setLocation(account.getLocation());
					created.setZone(account.getZone());
					created.setTariff(account.getTariff());
					created.setConsumer(consumer);
					accountRepository.save(created);

					// package response
					responseObject.setMessage("Account created successfully. ");
					responseObject.setPayload(created);
					response = new RestResponse(responseObject, HttpStatus.CREATED);
				}
			}
		} catch (Exception ex) {
			responseObject.setMessage(ex.getLocalizedMessage());
			response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
			log.error(ex.getLocalizedMessage());
		}
		return response;
	}

	public RestResponse update(RestRequestObject<Account> requestObject, Long id) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
				Account account = requestObject.getObject();
				Account acc = accountRepository.findOne(id);

				if (acc == null) {
					responseObject.setMessage("Account not found");
					response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
				} else {
					// setup resource
					// TODO;
					acc.setLocation(account.getLocation());
					acc.setZone(account.getZone());
					acc.setTariff(account.getTariff());

					acc.setAccNo(account.getAccNo());
					acc.setAverageConsumption(account.getAverageConsumption());
					acc.setBalanceBroughtForward(account.getBalanceBroughtForward());

					// save
					accountRepository.save(acc);
					responseObject.setMessage("Account  updated successfully");
					responseObject.setPayload(acc);
					response = new RestResponse(responseObject, HttpStatus.OK);
				}
			}
		} catch (org.hibernate.exception.ConstraintViolationException ex) {
			responseObject.setMessage("Duplicate account no");
			responseObject.setPayload("");
			response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
			log.error(ex.getLocalizedMessage());
		} catch (Exception ex) {
			responseObject.setMessage(ex.getLocalizedMessage());
			responseObject.setPayload("");
			response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
			log.error(ex.getLocalizedMessage());
		}
		return response;
	}

	public RestResponse transfer(RestRequestObject<Account> requestObject, Long id) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
				Account account = requestObject.getObject();

				Account acc = accountRepository.findOne(account.getAccountId());
				Consumer consumer = consumerRepository.findOne(id);
				if (acc == null) {
					responseObject.setMessage("Invalid account");
					response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
				} else if (consumer == null) {
					responseObject.setMessage("Invalid consumer");
					response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
				} else {
					// set new consumer
					acc.setConsumer(consumer);

					// save
					accountRepository.save(acc);
					responseObject.setMessage("Account  updated successfully");
					responseObject.setPayload(acc);
					response = new RestResponse(responseObject, HttpStatus.OK);
				}
			}
		} catch (org.hibernate.exception.ConstraintViolationException ex) {
			responseObject.setMessage("Duplicate account no");
			responseObject.setPayload("");
			response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
			log.error(ex.getLocalizedMessage());
		} catch (Exception ex) {
			responseObject.setMessage(ex.getLocalizedMessage());
			responseObject.setPayload("");
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

				Page<Account> page;
				if (p.getFilter().isEmpty()) {
					page = accountRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
				} else {
					page = accountRepository.findByAccNoLike(p.getFilter(), new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
				}
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

	public RestResponse getAllByConsumer(RestRequestObject<RestPageRequest> requestObject, Long consumerId) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
				Consumer consumer = consumerRepository.findOne(consumerId);
				if (consumer == null) {
					responseObject.setMessage("Invalid consumer info");
					response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
				} else {
					List<Account> accounts = consumer.getAccounts();
					if (accounts.size() > 0) {
						responseObject.setMessage("Fetched data successfully");
						responseObject.setPayload(accounts);
						response = new RestResponse(responseObject, HttpStatus.OK);
					} else {
						responseObject.setMessage("Your search did not match any records");
						responseObject.setPayload("");
						response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
					}
				}
			}
		} catch (Exception ex) {
			responseObject.setMessage(ex.getLocalizedMessage());
			response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
			log.error(ex.getLocalizedMessage());
			ex.printStackTrace();
		}
		return response;
	}

	public RestResponse getById(RestRequestObject<RestPageRequest> requestObject, Long id) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
				Account account = accountRepository.findOne(id);
				if (account == null) {
					responseObject.setMessage("Invalid account number");
					responseObject.setPayload("");
					response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
				} else {
					responseObject.setMessage("Fetched data successfully");
					responseObject.setPayload(account);
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

	public RestResponse getOne(RestRequestObject<Account> requestObject) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {

				Account acc = requestObject.getObject();
				Account account = accountRepository.findByaccNo(acc.getAccNo());

				if (account == null) {
					responseObject.setMessage("Invalid account number");
					responseObject.setPayload("");
					response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
				} else {
					responseObject.setMessage("Fetched data successfully");
					responseObject.setPayload(account);
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

}
