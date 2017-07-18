package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.SMSTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by manyala on 6/14/15.
 */
public interface SMSTemplateRepository extends PagingAndSortingRepository<SMSTemplate, Long> {

    Page<SMSTemplate> findAll(Pageable pageable);

    Page<SMSTemplate> findAllByNameContainsOrMessageContains(String name, String message, Pageable pageable);

    SMSTemplate findByName(String name);
}
