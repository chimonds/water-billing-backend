package ke.co.suncha.simba.aqua.repository;

import ke.co.suncha.simba.aqua.models.BillItem;
import ke.co.suncha.simba.aqua.models.BillItemType;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by manyala on 4/25/15.
 */
public interface BillItemRepository extends PagingAndSortingRepository<BillItem, Long> {

}
