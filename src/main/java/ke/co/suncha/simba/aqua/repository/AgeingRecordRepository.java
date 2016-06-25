package ke.co.suncha.simba.aqua.repository;


import ke.co.suncha.simba.aqua.models.AgeingRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by manyala on 6/18/15.
 */
public interface AgeingRecordRepository extends PagingAndSortingRepository<AgeingRecord, Long> {
    List<AgeingRecord> findAll();

    AgeingRecord findByAccNo(String accNo);
}
