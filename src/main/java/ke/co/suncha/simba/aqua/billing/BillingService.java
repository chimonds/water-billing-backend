package ke.co.suncha.simba.aqua.billing;

import ke.co.suncha.simba.aqua.utils.BillRequest;

import java.util.List;

/**
 * Created by maitha.manyala on 10/18/17.
 */
public interface BillingService {
    Double getBalanceBroughtForward(Long accountId);

    Double getBilledInMonth(Long accountId, Long billingMonthId);

    Long getTotalAccountsBilled(List<Long> zoneList, Long billingMonthId);

    Double getBilledInMonth(List<Long> zoneList, Long billingMonthId);

    Double getTotalBilledAmount(Long accountId);

    Double getLastMeterReading(Long accountId);

    Bill getById(Long billId);

    Bill getLastBill(Long accountId);

    Integer getMinimumAverageUnitsToBill();

    Bill create(BillRequest billRequest, Long accountId);
}
