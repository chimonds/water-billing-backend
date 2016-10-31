package ke.co.suncha.simba.aqua.reports.scheduled.monthly;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by maitha on 10/29/16.
 */
public interface SystemReportRepository extends PagingAndSortingRepository<SystemReport, Long>{
    List<SystemReport> findAllByStatus(Integer status);
}
