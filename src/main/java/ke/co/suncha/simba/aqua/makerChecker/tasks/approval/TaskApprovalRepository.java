package ke.co.suncha.simba.aqua.makerChecker.tasks.approval;

import ke.co.suncha.simba.aqua.makerChecker.tasks.Task;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by maitha on 11/21/16.
 */
public interface TaskApprovalRepository extends PagingAndSortingRepository<TaskApproval, Long>, QueryDslPredicateExecutor<TaskApproval> {
}
