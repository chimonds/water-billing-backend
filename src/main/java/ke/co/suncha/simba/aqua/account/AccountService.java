package ke.co.suncha.simba.aqua.account;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAUpdateClause;
import ke.co.suncha.simba.aqua.billing.BillingService;
import ke.co.suncha.simba.aqua.receipts.ReceiptService;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.services.BillingMonthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

/**
 * Created by maitha.manyala on 9/6/17.
 */
@Service
@Transactional
public class AccountService {
    @Autowired
    EntityManager entityManager;

    @Autowired
    BillingService billingService;

    @Autowired
    ReceiptService receiptService;

    @Autowired
    BillingMonthService billingMonthService;

    @Autowired
    AccountRepository accountRepository;

    public Double getReceiptsThisMonthCalculated(Long accountId) {
        Long billingMonthId = billingMonthService.getActiveMonthId();
        if (billingMonthId == null)
            return 9999999999.0;

        billingMonthId--;
        Double totalBilledAmount = billingService.getTotalBilledAmount(accountId);
        Double amountBilledInMonth = billingService.getBilledInMonth(accountId, billingMonthId);
        Double receipts = receiptService.getReceiptsToDate(accountId);
        Double totalOldBills = totalBilledAmount - amountBilledInMonth;
        Double balanceReceiptsToAllocate = receipts - totalOldBills;

        if (balanceReceiptsToAllocate < 0) {
            balanceReceiptsToAllocate = 0.0;
        } else {
            if (balanceReceiptsToAllocate > amountBilledInMonth) {
                balanceReceiptsToAllocate = amountBilledInMonth;
            }
        }
        return balanceReceiptsToAllocate;
    }

    public void updateAmountToAllocateThisMonth(Long accountId) {
        Double amount = getReceiptsThisMonthCalculated(accountId);
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QAccount.account.accountId.eq(accountId));
        JPAUpdateClause updateClause = new JPAUpdateClause(entityManager, QAccount.account);
        updateClause.where(builder);
        updateClause.set(QAccount.account.receiptsThisMonthCalculated, amount);
        updateClause.execute();
    }
}
