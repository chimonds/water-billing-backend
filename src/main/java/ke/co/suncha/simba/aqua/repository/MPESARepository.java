package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.MPESATransaction;
import ke.co.suncha.simba.aqua.models.Payment;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by manyala on 6/6/15.
 */
public interface MPESARepository extends PagingAndSortingRepository<MPESATransaction, Long> {
    MPESATransaction findByMpesaCode(String mpesaCode);

    List<MPESATransaction> findAllByAssigned(Boolean assigned);

}
