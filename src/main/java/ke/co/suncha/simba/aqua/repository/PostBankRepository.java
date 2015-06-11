package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.MPESATransaction;
import ke.co.suncha.simba.aqua.models.PostBankTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by manyala on 6/11/15.
 */
public interface PostBankRepository extends PagingAndSortingRepository<PostBankTransaction, Long> {
    PostBankTransaction findBySeqNo(String seqNo);

    List<PostBankTransaction> findAllByAssigned(Boolean assigned);

    Page<PostBankTransaction> findBySeqNoContainsOrAccount_AccNoContains(String mpesaAcc, String accNo, Pageable pageable);

    Page<PostBankTransaction> findAll(Pageable pageable);
}
