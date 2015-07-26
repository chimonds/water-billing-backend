package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.SMS;
import ke.co.suncha.simba.aqua.models.SMSGroup;
import ke.co.suncha.simba.aqua.models.SMSInquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by maitha.manyala on 7/23/15.
 */
public interface SMSInquiryRepository extends PagingAndSortingRepository<SMSInquiry, Long> {
    List<SMSInquiry> findAllBySend(Boolean send);

    SMSInquiry findBySequenceId(Integer sequenceId);

    @Transactional
    @Query(value = "SELECT MAX(sequence_id) FROM sms_inquiries", nativeQuery = true)
    Integer getMaxSequenceId();
}
