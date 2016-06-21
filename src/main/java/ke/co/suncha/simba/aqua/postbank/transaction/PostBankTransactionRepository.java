package ke.co.suncha.simba.aqua.postbank.transaction;

import ke.co.suncha.simba.aqua.models.PostBankTransaction;
import ke.co.suncha.simba.aqua.postbank.PostBankFile;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by maitha.manyala on 6/21/16.
 */
public interface PostBankTransactionRepository extends PagingAndSortingRepository<PostBankTransaction, Long> {
    PostBankTransaction findBySeqNo(String seqNo);

    List<PostBankTransaction> findAllByPostBankFile(PostBankFile postBankFile);
}
