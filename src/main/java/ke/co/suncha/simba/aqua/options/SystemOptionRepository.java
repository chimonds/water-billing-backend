package ke.co.suncha.simba.aqua.options;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by maitha on 10/24/16.
 */
public interface SystemOptionRepository extends PagingAndSortingRepository<SystemOption, Long> {
    SystemOption findByName(String name);
}
