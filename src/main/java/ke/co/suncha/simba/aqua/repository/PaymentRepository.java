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
import ke.co.suncha.simba.aqua.models.BillingMonth;
import ke.co.suncha.simba.aqua.models.Payment;

import ke.co.suncha.simba.aqua.models.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
public interface PaymentRepository extends PagingAndSortingRepository<Payment, Long> {

    Page<Payment> findAll(Pageable pageable);

    Page<Payment> findAllByReceiptNoContainsOrAccount_accNoContains(String receiptNo, String accNo, Pageable pageable);

    Page<Payment> findAllByAccount(Account account, Pageable pageable);

    Payment findByreceiptNo(String receiptNo);

    List<Payment> findByBillingMonth(BillingMonth billingMonth);

    List<Payment> findByBillingMonth_BillingMonthIdAndAccount(Long billingMonthId, Account account);

    List<Payment> findByTransactionDateBetweenOrderByTransactionDateDesc(Calendar from, Calendar to);

    List<Payment> findByTransactionDateBetween(Calendar from, Calendar to);

    @Query(value = "SELECT SUM(amount) FROM payments WHERE date(transaction_date)>=:startDate AND date(transaction_date)<=:endDate AND  payment_type_id IN(SELECT payment_type_id FROM payment_types WHERE is_unique=true)", nativeQuery = true)
    Double getTotalAmountPaidByDate(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Query(value = "SELECT SUM(amount) FROM payments WHERE date(transaction_date)=:transDate AND payment_type_id IN(SELECT payment_type_id FROM payment_types WHERE is_unique=true)", nativeQuery = true)
    Double getTotalAmountPaidByDate(@Param("transDate") String transDate);

    List<Payment> findByTransactionDateBetweenAndAccount(Calendar from, Calendar to, Account account);

    List<Payment> findByAccount_accountIdAndTransactionDateBetween(Long accountId, Calendar from, Calendar to);

    @Query(value = "select * from payments where account_id=:accountId AND transaction_date>=:startDate AND transaction_date<=:endDate", nativeQuery = true)
    List<Payment> findByTransactionDate(@Param("accountId") Long accountId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    List<Payment> findByTransactionDateBetweenAndPaymentType(Calendar from, Calendar to, PaymentType paymentType);

    @Query(value = "SELECT SUM(amount) FROM(SELECT payments.transaction_date, payments.account_id, payments.amount,accounts.zone_id, (SELECT name FROM zones WHERE zones.zone_id=accounts.zone_id) AS zone_code from payments LEFT JOIN (accounts) ON (accounts.account_id= payments.account_id)) AS temp WHERE zone_code =?1 AND transaction_date>=?2 AND transaction_date<=?3", nativeQuery = true)
    Double findByAmount(String zoneNo, String from, String to);


    @Query(value = "SELECT payment_id FROM payments WHERE account_id =?1", nativeQuery = true)
    List<BigInteger> findAllByAccount(Long accountId);

    @Query(value = "SELECT SUM(amount) FROM payments WHERE account_id =:accountId AND ref_no=:refNo", nativeQuery = true)
    Double findSumByRefNo(@Param("accountId") Long accountId, @Param("refNo") String refNo);

    @Query(value = "SELECT COUNT(payment_id) FROM payments WHERE receipt_no =:receiptNo", nativeQuery = true)
    Integer countByReceiptNo(@Param("receiptNo") String receiptNo);

    @Query(value = "SELECT SUM(amount) FROM payments WHERE account_id IN (SELECT account_id FROM accounts WHERE zone_id=:zoneId) AND date(transaction_date) >=:startDate AND date(transaction_date) <=:endDate AND payment_type_id IN(SELECT payment_type_id FROM payment_types WHERE is_unique=true)", nativeQuery = true)
    Double getTotalAmountByZoneByDate(@Param("zoneId") Long zoneId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    @Query(value = "SELECT SUM(amount) FROM payments WHERE payment_type_id=:paymentTypeId AND date(transaction_date) >=:startDate AND date(transaction_date) <=:endDate AND payment_type_id IN(SELECT payment_type_id FROM payment_types WHERE is_unique=true)", nativeQuery = true)
    Double getTotalAmountByPaymentTypeByDate(@Param("paymentTypeId") Long paymentTypeId, @Param("startDate") String startDate, @Param("endDate") String endDate);

}
