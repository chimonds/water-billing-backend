package ke.co.suncha.simba.admin.repositories;

import ke.co.suncha.simba.admin.models.AuditRecord;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by manyala on 5/21/15.
 */
public interface AuditRecordRepository extends PagingAndSortingRepository<AuditRecord, Long>, QueryDslPredicateExecutor<AuditRecord> {
}
