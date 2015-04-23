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
import ke.co.suncha.simba.aqua.models.Account;
import ke.co.suncha.simba.aqua.models.Bill;
import ke.co.suncha.simba.aqua.models.BillingMonth;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.repository.BillRepository;
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

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
 */
@Service
public class BillService {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BillRepository billRepository;

	@Autowired
	private AccountRepository accountRepository;

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

	public BillService() {
	}

	private Sort sortByDateAddedDesc() {
		return new Sort(Sort.Direction.DESC, "createdOn");
	}

	// private Bill getLastBill(Long accountId) {
	//
	// Bill lastBill = new Bill();
	// // get current billing month
	// BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);
	//
	// Account account = accountRepository.findOne(accountId);
	// Bill bill =
	// billRepository.findTopByAccountOrderByBillingMonth_MonthDesc(account);
	// if (bill == null) {
	// // seems its iniatial bill so check if account is metered
	// if (account.isMetered()) {
	// lastBill.setCurrentReading(account.getMeter().getInitialReading());
	// } else {
	// // TODO;
	// }
	// } else {
	// if (bill.getBillingMonth().getMonth().before(billingMonth.getMonth())) {
	// bill.setBilled(true);
	// } else {
	// lastBill = bill;
	// }
	// System.out.println("Bill Id:" + bill.getBillId());
	// }
	//
	// }

	public RestResponse getAllByAccount(RestRequestObject<RestPageRequest> requestObject, Long account_id) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {

				Account account = accountRepository.findOne(account_id);
				if (account == null) {
					responseObject.setMessage("Invalid account");
					responseObject.setPayload("");
					response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
				} else {

					RestPageRequest p = requestObject.getObject();

					Page<Bill> page;
					page = billRepository.findAllByAccount(account, new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));

					if (page.hasContent()) {

						responseObject.setMessage("Fetched data successfully");
						responseObject.setPayload(page);
						response = new RestResponse(responseObject, HttpStatus.OK);
					} else {
						responseObject.setMessage("Your search did not match any records");
						response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
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

	public RestResponse getLastBill(RestRequestObject<RestPageRequest> requestObject, Long accountId) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {

				Bill lastBill = new Bill();
				// get current billing month
				BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);

				Account account = accountRepository.findOne(accountId);

				Page<Bill> bills;
				bills = billRepository.findTop1ByAccountOrderByBillCodeDesc(account, new PageRequest(0, 1));

				if (!bills.hasContent()) {
					// seems its iniatial bill so check if account is metered
					if (account.isMetered()) {
						lastBill.setCurrentReading(account.getMeter().getInitialReading());
					} else {
						// TODO;
					}
				} else {

					lastBill = bills.getContent().get(0);

					if (lastBill.getBillingMonth().getMonth().before(billingMonth.getMonth())) {
						lastBill.setBilled(false);
					}
					System.out.println("Bill Id:" + lastBill.getBillId());
					responseObject.setMessage("Fetched data successfully");
					responseObject.setPayload(lastBill);
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
