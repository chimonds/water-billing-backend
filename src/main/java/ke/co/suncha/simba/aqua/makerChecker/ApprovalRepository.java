package ke.co.suncha.simba.aqua.makerChecker;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by maitha on 11/18/16.
 */
public interface ApprovalRepository extends PagingAndSortingRepository<Approval, Long>, QueryDslPredicateExecutor<Approval> {
}
