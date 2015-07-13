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
import ke.co.suncha.simba.aqua.models.Meter;

import ke.co.suncha.simba.aqua.models.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Transactional
public interface AccountRepository extends PagingAndSortingRepository<Account, Long> {

    Account findByaccNo(String accoutNo);

    Account findByMeter(Meter meter);

    @Query("select a from Account a where a.accNo like %?1%")
    Page<Account> findByAccNoLike(String accNo, Pageable pageable);

    Page<Account> findByAccNo(String accNo, Pageable pageable);

    List<Account> findAll();

    List<Account> findAllByZone(Zone zone);

    Long countByActive(Boolean status);

    @Transactional
    @Query(value = "SELECT consumer_id FROM accounts WHERE account_id =?1", nativeQuery = true)
    Long findConsumerIdByAccountId(Long accountId);

    @Transactional
    @Query(value = "SELECT zone_id FROM accounts WHERE account_id =?1", nativeQuery = true)
    Long findZoneIdByAccountId(Long accountId);


    @Transactional
    @Query(value = "SELECT acc_no FROM accounts", nativeQuery = true)
    List<String> findAllAccountNumbers();

}