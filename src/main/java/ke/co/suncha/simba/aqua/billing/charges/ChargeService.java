package ke.co.suncha.simba.aqua.billing.charges;

import com.mysema.query.BooleanBuilder;
import ke.co.suncha.simba.aqua.models.BillItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Created by maitha.manyala on 10/12/17.
 */
public interface ChargeService {
    Charge create(Long userId, Long accountId, List<BillItemType> billItemTypeList);

    void delete(Long chargeId);

    Boolean accountHasCharge(Long accountId);

    Boolean exists(Long chargeId);

    Boolean canDelete(Long chargeId);

    Charge getById(Long chargeId);

    Charge get(Long accountId, Long billingMonthId);

    Page<Charge> getPage(BooleanBuilder builder, PageRequest pageRequest);
}
