package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.AgeingData;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by maitha.manyala on 7/12/16.
 */
public interface AgeingDataRepository extends PagingAndSortingRepository<AgeingData, Long>,QueryDslPredicateExecutor<AgeingData> {
    List<AgeingData> findAll();
}
