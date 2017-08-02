package ke.co.suncha.simba.aqua.sms;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by maitha.manyala on 7/27/17.
 */
public interface SMSInboxRepository extends PagingAndSortingRepository<SMSInbox, Long>, QueryDslPredicateExecutor<SMSInbox> {
}
