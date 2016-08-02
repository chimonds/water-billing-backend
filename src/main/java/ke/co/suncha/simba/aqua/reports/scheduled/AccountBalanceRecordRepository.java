package ke.co.suncha.simba.aqua.reports.scheduled;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Created by maitha.manyala on 8/1/16.
 */
public interface AccountBalanceRecordRepository extends PagingAndSortingRepository<AccountBalanceRecord, Long>, QueryDslPredicateExecutor<AccountBalanceRecord> {
    @Query(value = "SELECT COUNT(account_balance_record_id) FROM account_balance_records WHERE account_id =:accountId  AND report_header_id=:reportHeaderId", nativeQuery = true)
    Integer exists(@Param("accountId") Long accountId, @Param("reportHeaderId") Long reportHeaderId);
}
