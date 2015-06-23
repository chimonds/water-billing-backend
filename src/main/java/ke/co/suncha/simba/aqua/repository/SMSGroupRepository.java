package ke.co.suncha.simba.aqua.repository;

import com.sun.org.apache.xpath.internal.operations.Bool;
import ke.co.suncha.simba.aqua.models.SMS;
import ke.co.suncha.simba.aqua.models.SMSGroup;
import ke.co.suncha.simba.aqua.models.SMSTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by manyala on 6/21/15.
 */
public interface SMSGroupRepository extends PagingAndSortingRepository<SMSGroup, Long> {
    Page<SMSGroup> findAll(Pageable pageable);

    Page<SMSGroup> findAllByNameContains(String name, Pageable pageable);

    SMSGroup findByName(String name);

    List<SMSGroup> findAll();

    List<SMSGroup> findAllByExploded(Boolean exploded);

    List<SMSGroup> findAllByApprovedAndExploded(Boolean approved, Boolean exploded);
}
