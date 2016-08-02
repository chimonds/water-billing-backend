package ke.co.suncha.simba.aqua.reports.scheduled;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Created by maitha.manyala on 8/1/16.
 */
public interface ReportHeaderRepository extends PagingAndSortingRepository<ReportHeader, Long>, QueryDslPredicateExecutor<ReportHeader> {
    @Query(value = "SELECT COUNT(report_header_id) FROM report_headers WHERE requested_by =:requestedBy  AND status=:status", nativeQuery = true)
    Integer countPendingByRequestor(@Param("requestedBy") String requestedBy, @Param("status") Integer status);
}
