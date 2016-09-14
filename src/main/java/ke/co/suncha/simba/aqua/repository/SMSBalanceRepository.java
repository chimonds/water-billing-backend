package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.SMSBalance;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by maitha.manyala on 9/2/16.
 */
public interface SMSBalanceRepository extends PagingAndSortingRepository<SMSBalance, Long>, QueryDslPredicateExecutor<SMSBalance> {
}
