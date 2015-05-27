package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.SMS;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> Created on 5/26/15.
 */
public interface SMSRepository extends PagingAndSortingRepository<SMS, Long> {
    List<SMS> findAllBySend(Boolean send);
}
