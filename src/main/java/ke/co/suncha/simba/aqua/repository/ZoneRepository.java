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

import ke.co.suncha.simba.aqua.account.scheme.Scheme;
import ke.co.suncha.simba.aqua.models.Zone;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
public interface ZoneRepository extends PagingAndSortingRepository<Zone, Long> {
    Page<Zone> findByNameContains(String name, Pageable pageable);

    Zone findByName(String name);

    Page<Zone> findAll(Pageable pageable);

    List<Zone> findAll();

    List<Zone> findAllByScheme(Scheme scheme);

    @Query(value = "SELECT name FROM zones WHERE zone_id = (SELECT zone_id FROM accounts WHERE account_id=:accountId)", nativeQuery = true)
    String getByAccountId(@Param("accountId") Long accountId);
}