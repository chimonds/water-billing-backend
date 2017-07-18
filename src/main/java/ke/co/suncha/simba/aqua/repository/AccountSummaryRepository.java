package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.AccountSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by maitha.manyala on 7/9/15.
 */
public interface AccountSummaryRepository extends PagingAndSortingRepository<AccountSummary, Long> {

    AccountSummary findByaccNo(String accoutNo);

    List<AccountSummary> findAll();

    Page<AccountSummary> findAllByNotifyClient(Boolean notifyClient, Pageable pageable);
}
