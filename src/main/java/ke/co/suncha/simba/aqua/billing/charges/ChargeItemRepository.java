package ke.co.suncha.simba.aqua.billing.charges;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by maitha.manyala on 10/12/17.
 */
public interface ChargeItemRepository  extends PagingAndSortingRepository<ChargeItem, Long>, QueryDslPredicateExecutor<ChargeItem> {
}
