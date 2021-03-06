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

import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.models.Meter;

import ke.co.suncha.simba.aqua.scheme.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Transactional
public interface AccountRepository extends PagingAndSortingRepository<Account, Long>, QueryDslPredicateExecutor<Account> {

    Account findByaccNo(String accoutNo);

    Account findByMeter(Meter meter);

    @Query("select a from Account a where a.accNo like %?1%")
    Page<Account> findByAccNoLike(String accNo, Pageable pageable);

    Page<Account> findByAccNo(String accNo, Pageable pageable);

   // List<Account> findAll();

    //List<Account> findAll(BooleanBuilder booleanBuilder);


    List<Account> findAllByZone(Zone zone);

    Long countByActive(Boolean status);

    @Transactional
    @Query(value = "SELECT consumer_id FROM accounts WHERE account_id =?1", nativeQuery = true)
    Long findConsumerIdByAccountId(Long accountId);

    @Transactional
    @Query(value = "SELECT balance_bf FROM accounts WHERE account_id =?1", nativeQuery = true)
    Double getBalanceBf(Long accountId);

    @Transactional
    @Query(value = "SELECT zone_id FROM accounts WHERE account_id =?1", nativeQuery = true)
    Long findZoneIdByAccountId(Long accountId);

    @Transactional
    @Query(value = "SELECT meter_id FROM accounts WHERE account_id =?1", nativeQuery = true)
    Long findMeterIdByAccountId(Long accountId);


    @Transactional
    @Query(value = "SELECT acc_no FROM accounts", nativeQuery = true)
    List<String> findAllAccountNumbers();

    @Transactional
    @Query(value = "SELECT account_id FROM accounts", nativeQuery = true)
    List<BigInteger> findAllAccountIds();

    @Transactional
    @Query(value = "SELECT account_id FROM accounts WHERE zone_id =:zoneId", nativeQuery = true)
    List<BigInteger> findAllAccountNumbersByZoneId(@Param("zoneId") Long zoneId);

    @Transactional
    @Query(value = "SELECT SUM(outstanding_balance) FROM accounts WHERE cut_off =?1", nativeQuery = true)
    Double getOutstandingBalancesByStatus(Integer cutOff);


    @Transactional
    @Query(value = "SELECT SUM(outstanding_balance) FROM accounts WHERE outstanding_balance>0", nativeQuery = true)
    Double getAllBalances();

    @Transactional
    @Query(value = "SELECT SUM(outstanding_balance) FROM accounts WHERE outstanding_balance<0", nativeQuery = true)
    Double getAllCreditBalances();


    @Transactional
    @Query(value = "SELECT count(*) FROM accounts WHERE cut_off=1 AND meter_id is not null AND  date(created_on)<=:createdOn", nativeQuery = true)
    Integer getActiveMeteredAccounts(@Param("createdOn") String createdOn);

    @Transactional
    @Query(value = "SELECT count(*) FROM accounts WHERE cut_off=1 AND meter_id is null AND date(created_on)<=:createdOn", nativeQuery = true)
    Integer getActiveUnMeteredAccounts(@Param("createdOn") String createdOn);


    @Query(value = "SELECT SUM(outstanding_balance) FROM accounts WHERE cut_off =:cutOff AND zone_id=:zoneId AND outstanding_balance>0", nativeQuery = true)
    Double getBalancesByStatusByZone(@Param("zoneId") Long zoneId, @Param("cutOff") Integer cutOff);

    @Query(value = "SELECT consumer_id FROM accounts WHERE account_id=:accountId", nativeQuery = true)
    Long getCustomerId(@Param("accountId") Long accountId);

    @Query(value = "SELECT acc_no FROM accounts WHERE account_id=:accountId", nativeQuery = true)
    String getAccountNo(@Param("accountId") Long accountId);

    @Query(value = "SELECT cut_off FROM accounts WHERE account_id=:accountId", nativeQuery = true)
    Integer getAccountStatus(@Param("accountId") Long accountId);

    @Query(value = "SELECT COUNT(account_id) FROM accounts WHERE date(created_on)<=:createdOn AND cut_off=:cutOff", nativeQuery = true)
    Long getCountByCreatedOn(@Param("createdOn") String createdOn, @Param("cutOff") Integer cutOff);

    @Query(value = "SELECT SUM(outstanding_balance) FROM accounts WHERE date(created_on)<=:createdOn AND cut_off=:cutOff AND outstanding_balance>0", nativeQuery = true)
    Double getTotalBalanceByCreatedOn(@Param("createdOn") String createdOn, @Param("cutOff") Integer cutOff);
}