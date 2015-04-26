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

import ke.co.suncha.simba.Application;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.aqua.models.Account;
import ke.co.suncha.simba.aqua.models.Tariff;
import ke.co.suncha.simba.aqua.models.TariffMatrix;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.repository.TariffRepository;

import ke.co.suncha.simba.aqua.utils.BillMeta;
import ke.co.suncha.simba.aqua.utils.TariffRateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Service
public class TariffService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public TariffService() {

    }


    public RestResponse calculate(RestRequestObject<BillMeta> requestObject, Long accountId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                BillMeta bm = requestObject.getObject();
                if (bm == null) {
                    responseObject.setMessage("Invalid bill meta");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                }
                BillMeta result = this.calculate(bm, accountId);
                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(result);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }


    public BillMeta calculate(BillMeta billMeta, Long accountId) {
        BillMeta result = new BillMeta();

        Integer unitsConsumed = billMeta.getUnits();

        Account account = accountRepository.findOne(accountId);
        if (account == null) {
            return result;
        }
        log.info("Calculating bill for:" + account.getAccNo());
        Double amount = 0.0;
        String content = "";

        List<TariffMatrix> matrices = account.getTariff().getTariffMatrixes();
        if (matrices.size() > 0) {
            for (TariffMatrix tm : matrices) {
                Integer matrixRange = tm.getMaximum() - tm.getMinimum();
                if (tm.getMinimum() > 0) {
                    matrixRange += 1;
                }

                log.info("Matrix range:" + matrixRange);

                Integer unitsBillable = 0;
                if (unitsConsumed >= matrixRange) {
                    unitsBillable = matrixRange;
                } else {
                    unitsBillable = unitsConsumed;
                }
                log.info("Units Billable:" + unitsBillable);

                //Update Units Consumed
                unitsConsumed -= unitsBillable;

                if (unitsBillable > 0) {
                    log.info("Tariff Type:" + tm.getRateType());
                    Double thisAmount = 0.0;
                    if (tm.getRateType().compareToIgnoreCase("FLAT") == 0) {
                        amount += tm.getAmount();
                        thisAmount = tm.getAmount();
                    } else if (tm.getRateType().compareToIgnoreCase("PER_UNIT") == 0) {
                        amount += (unitsBillable * tm.getAmount());
                        thisAmount = unitsBillable * tm.getAmount();
                    }

                    if (tm.getMinimum() == 0) {
                        content = content + unitsBillable + " Min KES " + tm.getAmount() + " = " + thisAmount + " \\n";
                    } else {
                        content = content + unitsBillable + " @ KES " + tm.getAmount() + " = " + thisAmount + " \\n";
                    }
                    log.info("Current Billed Amount:" + amount);
                }
            }
        }
        //set amount
        result.setAmount(amount);
        result.setContent(content);
        log.info("Bill Amount:" + amount);
        log.info("Bill Content:" + content);
        log.info("********************************************");
        return result;
    }

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {

                RestPageRequest p = requestObject.getObject();

                Page<Tariff> page;
                page = tariffRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));

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
