package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.MPESATransaction;
import ke.co.suncha.simba.aqua.models.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by manyala on 6/6/15.
 */
public interface MPESARepository extends PagingAndSortingRepository<MPESATransaction, Long> {
    MPESATransaction findByMpesacode(String mpesaCode);

    List<MPESATransaction> findAllByAssigned(Boolean assigned);

    Page<MPESATransaction> findByMpesaaccContainsOrAccount_AccNoContainsOrMpesacodeContains(String mpesaAcc, String accNo, String mpesaCode, Pageable pageable);

    Page<MPESATransaction> findAll(Pageable pageable);

    @Query(value = "SELECT SUM(mpesa_amt) FROM mpesa_transactions WHERE allocated =?1", nativeQuery = true)
    Double findSumAllocated(Integer allocated);
}
