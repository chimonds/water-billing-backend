package ke.co.suncha.simba.aqua.makerChecker.tasks;

import ke.co.suncha.simba.aqua.makerChecker.type.TaskType;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by maitha on 11/20/16.
 */
public interface TaskRepository extends PagingAndSortingRepository<Task, Long>, QueryDslPredicateExecutor<Task> {
}
