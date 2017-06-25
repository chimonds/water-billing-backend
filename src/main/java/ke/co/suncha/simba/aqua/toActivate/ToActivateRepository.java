package ke.co.suncha.simba.aqua.toActivate;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by maitha.manyala on 6/6/17.
 */
public interface ToActivateRepository  extends PagingAndSortingRepository<ToActivate, Long>, QueryDslPredicateExecutor<ToActivate> {
}
