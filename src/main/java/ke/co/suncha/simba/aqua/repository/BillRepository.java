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
package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.Account;
import ke.co.suncha.simba.aqua.models.Bill;
import ke.co.suncha.simba.aqua.models.BillingMonth;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */

public interface BillRepository extends PagingAndSortingRepository<Bill, Long> {
    Page<Bill> findAll(Pageable pageable);

    Page<Bill> findAllByAccount(Account account, Pageable pageable);

    Page<Bill> findByAccountOrderByBillCodeDesc(Account account, Pageable pageable);

    List<Bill> findAllByConsumptionTypeAndBillingMonth(String consumptionType, BillingMonth billingMonth);

//    List<Bill> findAllByBillingMonth(BillingMonth billingMonth);

    @Query(value = "SELECT bill_id FROM bills WHERE billing_month_id =?1", nativeQuery = true)
    List<BigInteger> findAllByBillingMonth(Long billingMonthId);

    @Query(value = "SELECT bill_id FROM bills WHERE account_id =?1", nativeQuery = true)
    List<BigInteger> findAllByAccount(Long accountId);

    @Query(value = "SELECT bill_id FROM bills WHERE billing_month_id =?1 AND account_id=?2", nativeQuery = true)
    List<BigInteger> findAllByBillingMonthAndAccount_AccNo(Long billingMonthId, Long accountId);

    List<Bill> findAllByBillingMonth_BillingMonthId(Long billingMonthId);
//    List<Bill> findAllByBillingMonthAndAccount_AccNo(BillingMonth billingMonth, String accNo);

    @Query(value = "SELECT SUM(amount) FROM(SELECT bills.billing_month_id, (SELECT billing_month FROM billing_months WHERE bills.billing_month_id=billing_months.billing_month_id) AS billing_month, bills.account_id, bills.amount,accounts.zone_id, (SELECT name FROM zones WHERE zones.zone_id=accounts.zone_id) AS zone_code from bills LEFT JOIN (accounts) ON (accounts.account_id= bills.account_id)) AS temp WHERE zone_code =?1 AND billing_month>=?2 AND billing_month<=?3", nativeQuery = true)
    Double findByContent(String zoneNo, String from, String to);

    //@Transactional
    @Query(value = "SELECT account_id FROM bills WHERE bill_id =?1", nativeQuery = true)
    Long findAccountIdByBillId(Long billId);

    //@Transactional
    @Query(value = "select max(bill_code) bill_id from bills  WHERE account_id =?1 AND bill_code<?2", nativeQuery = true)
    Integer findPreviousBillCode(Long accountId, Integer billCode);

    //@Transactional
    @Query(value = "SELECT transaction_date FROM bills  WHERE account_id =?1 AND bill_code=?2", nativeQuery = true)
    Timestamp findPreviousBillDate(Long accountId, Integer billCode);
}
