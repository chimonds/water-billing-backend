package ke.co.suncha.simba.aqua.postbank;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by maitha.manyala on 6/21/16.
 */
public interface PostBankFileRepository extends PagingAndSortingRepository<PostBankFile, Long>, QueryDslPredicateExecutor<PostBankFile> {
    Page<PostBankFile> findAllByNameContains(String name, Pageable pageable);
}
