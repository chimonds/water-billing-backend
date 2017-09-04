package ke.co.suncha.simba.mobile.upload;

import ke.co.suncha.simba.aqua.models.MeterReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by maitha.manyala on 7/26/15.
 */
public interface MeterReadingRepository extends PagingAndSortingRepository<MeterReading, Long>, QueryDslPredicateExecutor<MeterReading> {

    //Page<MeterReading> findByAccNoContains(String accNo, Pageable pageable);

    Page<MeterReading> findAll(Pageable pageable);
}