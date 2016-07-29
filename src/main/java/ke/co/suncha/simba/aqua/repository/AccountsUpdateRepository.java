package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.AccountUpdate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by maitha.manyala on 7/21/16.
 */
@Transactional
public interface AccountsUpdateRepository extends PagingAndSortingRepository<AccountUpdate, Long> {
//    @Transactional
//    List<AccountUpdate> findAllByStatus(Integer status);

    @Transactional
    @Query(value = "SELECT record_id FROM accounts_to_update WHERE status=0 LIMIT 10", nativeQuery = true)
    List<BigInteger> findAllByPending();
}
