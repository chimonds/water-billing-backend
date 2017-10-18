package ke.co.suncha.simba.aqua.billing.validator;

import ke.co.suncha.simba.aqua.utils.BillRequest;
import ke.co.suncha.simba.aqua.utils.Response;

/**
 * Created by maitha.manyala on 10/18/17.
 */
public interface BillValidator {
    Response create(BillRequest billRequest, Long accountId);
}
