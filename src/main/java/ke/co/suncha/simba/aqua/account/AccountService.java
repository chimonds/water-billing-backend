package ke.co.suncha.simba.aqua.account;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
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

    public Double getTotalBalances() {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QAccount.account.outstandingBalance.gt(0d));
        JPAQuery query = new JPAQuery(entityManager);
        Double amount = query.from(QAccount.account).where(builder).singleResult(QAccount.account.outstandingBalance.sum());
        if (amount == null) {
            amount = 0.0d;
        }
        return amount;
    }

    public Long getTotalActiveAccounts() {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QAccount.account.onStatus.eq(1));
        builder.and(QAccount.account.active.eq(Boolean.TRUE));
        JPAQuery query = new JPAQuery(entityManager);
        Long count = query.from(QAccount.account).where(builder).singleResult(QAccount.account.accountId.count());
        if (count == null) {
            count = 0L;
        }
        return count;
    }

    public Long getTotalInActiveAccounts() {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QAccount.account.onStatus.eq(1));
        builder.and(QAccount.account.active.eq(Boolean.FALSE));
        JPAQuery query = new JPAQuery(entityManager);
        Long count = query.from(QAccount.account).where(builder).singleResult(QAccount.account.accountId.count());
        if (count == null) {
            count = 0L;
        }
        return count;
    }

    public Long getTotalAccounts() {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QAccount.account.onStatus.eq(1));
        JPAQuery query = new JPAQuery(entityManager);
        Long count = query.from(QAccount.account).where(builder).singleResult(QAccount.account.accountId.count());
        if (count == null) {
            count = 0L;
        }
        return count;
    }

    public Double getTotalCreditBalances() {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QAccount.account.outstandingBalance.lt(0d));
        JPAQuery query = new JPAQuery(entityManager);
        Double amount = query.from(QAccount.account).where(builder).singleResult(QAccount.account.outstandingBalance.sum());
        if (amount == null) {
            amount = 0.0d;
        }
        return amount;
    }

    public Double getReceiptsThisMonthCalculated(Long accountId) {
        Long billingMonthId = billingMonthService.getActiveMonthId();
        if (billingMonthId == null) {
            return 9999999999.0;
        } else {
            billingMonthId = billingMonthId - 1;
        }
        Double totalBilledAmount = billingService.getTotalBilledAmount(accountId);
        Double amountBilledInMonth = billingService.getBilledInMonth(accountId, billingMonthId);
        Double receiptsToDate = receiptService.getReceiptsToDate(accountId);
        Double totalOldBills = totalBilledAmount - amountBilledInMonth;
        Double balanceReceiptsToAllocate = receiptsToDate - totalOldBills;

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
