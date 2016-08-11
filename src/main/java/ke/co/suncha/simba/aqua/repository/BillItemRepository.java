package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.BillItem;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by manyala on 4/25/15.
 */
public interface BillItemRepository extends PagingAndSortingRepository<BillItem, Long>, QueryDslPredicateExecutor<BillItem> {

}
