package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.AccountCategory;
import ke.co.suncha.simba.aqua.models.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by maitha.manyala on 2/22/16.
 */
public interface AccountCategoryRepository extends PagingAndSortingRepository<AccountCategory, Long> {
    Page<AccountCategory> findByNameContains(String name, Pageable pageable);

    AccountCategory findByName(String name);

    Page<AccountCategory> findAll(Pageable pageable);

    List<AccountCategory> findAll();
}
