package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.Meter;
import ke.co.suncha.simba.aqua.models.MeterReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by maitha.manyala on 7/26/15.
 */
public interface MeterReadingRepository extends PagingAndSortingRepository<MeterReading, Long> {

    Page<MeterReading> findByAccNoContains(String accNo, Pageable pageable);

    MeterReading findByReferenceCode(Integer referenceCode);

    Page<MeterReading> findAll(Pageable pageable);
}