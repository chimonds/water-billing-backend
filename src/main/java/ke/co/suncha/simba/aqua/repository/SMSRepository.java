package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.SMS;
import ke.co.suncha.simba.aqua.models.SMSGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com> Created on 5/26/15.
 */
public interface SMSRepository extends PagingAndSortingRepository<SMS, Long> {
    List<SMS> findAllBySendAndIsVoid(Boolean send, Boolean isVoid);

    Page<SMS> findAll(Pageable pageable);

    Page<SMS> findAllByMessageContainsOrMobileNumberContains(String name, String mobileNumber, Pageable pageable);

    SMS findBySmsGroupAndMobileNumber(SMSGroup smsGroup, String mobileNumber);

}
