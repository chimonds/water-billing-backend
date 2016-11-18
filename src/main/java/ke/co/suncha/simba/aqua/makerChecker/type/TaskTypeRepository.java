package ke.co.suncha.simba.aqua.makerChecker.type;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by maitha on 11/16/16.
 */
public interface TaskTypeRepository extends PagingAndSortingRepository<TaskType, Long>, QueryDslPredicateExecutor<TaskType> {
}
