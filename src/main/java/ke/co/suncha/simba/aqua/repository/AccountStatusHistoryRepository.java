package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.AccountStatusHistory;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by manyala on 6/2/15.
 */
public interface AccountStatusHistoryRepository extends PagingAndSortingRepository<AccountStatusHistory, Long> {
}
