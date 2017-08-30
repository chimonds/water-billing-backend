package ke.co.suncha.simba.aqua.scheme;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by maitha.manyala on 7/27/16.
 */
public interface SchemeRepository extends PagingAndSortingRepository<Scheme, Long> {
    List<Scheme> findAll();
}
