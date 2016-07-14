package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.AgeingData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by maitha.manyala on 7/12/16.
 */
public interface AgeingDataRepository extends PagingAndSortingRepository<AgeingData, Long> {
    @Query(value = "SELECT record_id FROM ageing_data WHERE account_id=:accountId AND user_id=:userId", nativeQuery = true)
    Long getRecordId(@Param("accountId") Long accountId, @Param("userId") Long userId);

    List<AgeingData> findAll();
}
