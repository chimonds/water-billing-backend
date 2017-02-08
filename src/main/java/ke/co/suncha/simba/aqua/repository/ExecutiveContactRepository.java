package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.ExecutiveContact;
import ke.co.suncha.simba.aqua.models.Location;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by maitha.manyala on 2/8/17.
 */
public interface ExecutiveContactRepository extends PagingAndSortingRepository<Location, Long>, QueryDslPredicateExecutor<ExecutiveContact> {
}
