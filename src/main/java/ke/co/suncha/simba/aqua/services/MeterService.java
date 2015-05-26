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

import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.aqua.models.Account;
import ke.co.suncha.simba.aqua.models.Meter;
import ke.co.suncha.simba.aqua.models.MeterAllocation;
import ke.co.suncha.simba.aqua.models.MeterOwner;
import ke.co.suncha.simba.aqua.models.MeterSize;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.repository.MeterAllocationRepository;
import ke.co.suncha.simba.aqua.repository.MeterOwnerRepository;
import ke.co.suncha.simba.aqua.repository.MeterRepository;
import ke.co.suncha.simba.aqua.repository.MeterSizeRepository;

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

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
 */
@Service
public class MeterService {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private MeterRepository meterRepository;

	@Autowired
	private MeterAllocationRepository meterAllocationRepository;

	@Autowired
	private MeterOwnerRepository meterOwnerRepository;

	@Autowired
	private MeterSizeRepository meterSizeRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private AuthManager authManager;

	@Autowired
	CounterService counterService;

	@Autowired
	GaugeService gaugeService;

	@Autowired
	private AuditService auditService;

	private RestResponse response;
	private RestResponseObject responseObject = new RestResponseObject();

	public MeterService() {

	}

	@Transactional
	public RestResponse create(RestRequestObject<Meter> requestObject) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {

				response = authManager.grant(requestObject.getToken(), "meter_create");
				if (response.getStatusCode() != HttpStatus.OK) {
					return response;
				}

				Meter meter = requestObject.getObject();
				Meter m = meterRepository.findByMeterNo(meter.getMeterNo());
				if (m != null) {
					responseObject.setMessage("Meter already exists");
					response = new RestResponse(responseObject, HttpStatus.CONFLICT);
				} else {
					// create resource

					MeterOwner meterOwner = meterOwnerRepository.findOne(meter.getMeterOwner().getMeterOwnerId());
					MeterSize meterSize = meterSizeRepository.findOne(meter.getMeterSize().getMeterSizeId());

					meter.setMeterOwner(meterOwner);
					meter.setMeterSize(meterSize);

					Meter created = meterRepository.save(meter);

					// package response
					responseObject.setMessage("Meter created successfully. ");
					responseObject.setPayload(created);
					response = new RestResponse(responseObject, HttpStatus.CREATED);

					//Start - audit trail
					AuditRecord auditRecord = new AuditRecord();
					auditRecord.setParentID(String.valueOf(created.getMeterId()));
					auditRecord.setCurrentData(created.toString());
					auditRecord.setParentObject("Meters");
					auditRecord.setNotes("CREATED METER");
					auditService.log(AuditOperation.CREATED, auditRecord);
					//End - audit trail
				}
			}
		} catch (Exception ex) {
			responseObject.setMessage(ex.getLocalizedMessage());
			response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
			log.error(ex.getLocalizedMessage());
		}
		return response;
	}

	@Transactional
	public RestResponse deallocate(RestRequestObject<Meter> requestObject, Long id) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "meter_deallocate");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

				Meter m = meterRepository.findOne(id);
				if (m == null) {
					responseObject.setMessage("Meter not found");
					response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
				} else {
					// setup resource
					if (m.getAccount() == null) {
						responseObject.setMessage("Meter not allocated to any connection.");
						responseObject.setPayload("");
						response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);

					} else {
						// update meter info
						Meter meter = requestObject.getObject();

						// Update account info
						Account acc = accountRepository.findByMeter(m);
						acc.setMeter(null);
						accountRepository.save(acc); // persist changes

						// update meter info
						m.setNotes(meter.getNotes());
						meterRepository.save(m); // persist changes

						// update meter allocation
						MeterAllocation meterAllocation = new MeterAllocation();
						meterAllocation.setAccount(acc);
						meterAllocation.setMeter(m);
						meterAllocation.setNotes(meter.getNotes());
						meterAllocation.setAllocationType("DeAllocated");
						meterAllocation.setReading(m.getInitialReading());
						meterAllocationRepository.save(meterAllocation);

						// meterRepository.save(m);
						// meterAllocationRepository.save(meterAllocation);

						responseObject.setMessage("Meter  allocation updated successfully");
						responseObject.setPayload(m);
						response = new RestResponse(responseObject, HttpStatus.OK);

						//Start - audit trail
//						AuditRecord auditRecord = new AuditRecord();
//						auditRecord.setParentID(String.valueOf(m.getMeterId()));
//						auditRecord.setCurrentData(m.getAccount().getAccNo());
//						auditRecord.setParentObject("Meters");
//						auditRecord.setNotes("METER DEALLOCATION");
//						auditService.log(AuditOperation.UPDATED, auditRecord);
						//End - audit trail
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

	@Transactional
	public RestResponse allocate(RestRequestObject<Meter> requestObject, Long id) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "meter_allocate");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

				Meter m = meterRepository.findOne(id);
				if (m == null) {
					responseObject.setMessage("Meter not found");
					response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
				} else {
					// setup resource
					if (m.getAccount() != null) {
						responseObject.setMessage("Meter already allocated to " + m.getAccount().getAccNo() + ".  Please deallocate the meter.");
						responseObject.setPayload("");
						response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);

					} else {
						// update meter info
						Meter meter = requestObject.getObject();

						// set account
						Account acc = accountRepository.findByaccNo(meter.getAccountId());
						if (acc.getMeter() != null) {
							responseObject.setMessage("You can not allocate a meter to this connection. The connection is already allocated to meter  " + acc.getMeter().getMeterNo());
							responseObject.setPayload("");
							response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);

						} else {
							acc.setMeter(m);
							accountRepository.save(acc); // persist account info

							// update meter allocations
							MeterAllocation meterAllocation = new MeterAllocation();
							meterAllocation.setAccount(acc);
							meterAllocation.setMeter(m);
							meterAllocation.setNotes(meter.getNotes());
							meterAllocation.setAllocationType("Allocated");
							meterAllocation.setReading(m.getInitialReading());
							meterAllocationRepository.save(meterAllocation);

							responseObject.setMessage("Meter  allocation updated successfully");
							responseObject.setPayload(m);
							response = new RestResponse(responseObject, HttpStatus.OK);

							//Start - audit trail
//							AuditRecord auditRecord = new AuditRecord();
//							auditRecord.setParentID(String.valueOf(m.getMeterId()));
//							auditRecord.setCurrentData(m.getAccount().getAccNo());
//							auditRecord.setParentObject("Meters");
//							auditRecord.setNotes("METER ALLOCATION");
//							auditService.log(AuditOperation.UPDATED, auditRecord);
							//End - audit trail
						}
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

	@Transactional
	public RestResponse update(RestRequestObject<Meter> requestObject) {
		try {
			response = authManager.tokenValid(requestObject.getToken());
			if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "meter_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

				Meter meter = requestObject.getObject();
				Meter m = meterRepository.findOne(meter.getMeterId());
				if (m == null) {
					responseObject.setMessage("Meter not found");
					response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
				} else {
					// setup resource
					m.setMeterNo(meter.getMeterNo());
					m.setInitialReading(meter.getInitialReading());

					MeterOwner meterOwner = meterOwnerRepository.findOne(meter.getMeterOwner().getMeterOwnerId());
					MeterSize meterSize = meterSizeRepository.findOne(meter.getMeterSize().getMeterSizeId());

					m.setMeterOwner(meterOwner);
					m.setMeterSize(meterSize);
					// save
					meterRepository.save(m);
					responseObject.setMessage("Meter  updated successfully");
					responseObject.setPayload(m);
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
                response = authManager.grant(requestObject.getToken(), "meter_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

				RestPageRequest p = requestObject.getObject();

				Page<Meter> page;
				if (p.getFilter().isEmpty()) {
					page = meterRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
				} else {

					page = meterRepository.findByMeterNoContainsOrAccount_AccNoContains(p.getFilter(), p.getFilter(),new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));

				}
				if (page.hasContent()) {

					responseObject.setMessage("Fetched data successfully");
					responseObject.setPayload(page);
					response = new RestResponse(responseObject, HttpStatus.OK);
				} else {
					responseObject.setMessage("Your search did not match any records");
					response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
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
