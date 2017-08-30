package ke.co.suncha.simba.aqua.scheme.zone.meterReader;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by maitha.manyala on 8/30/17.
 */
public interface MeterReaderRepository extends PagingAndSortingRepository<MeterReader, Long>, QueryDslPredicateExecutor<MeterReader> {

}