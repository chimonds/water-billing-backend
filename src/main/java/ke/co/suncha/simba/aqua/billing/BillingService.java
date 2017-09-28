package ke.co.suncha.simba.aqua.billing;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.aqua.account.QAccount;
import ke.co.suncha.simba.aqua.services.BillingMonthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

/**
 * Created by maitha.manyala on 9/6/17.
 */
@Service
public class BillingService {
    @Autowired
    EntityManager entityManager;

    @Autowired
    BillingMonthService billingMonthService;

    public Double getBalanceBroughtForward(Long accountId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QAccount.account.accountId.eq(accountId));
        JPAQuery query = new JPAQuery(entityManager);
        Double balanceBF = query.from(QAccount.account).where(builder).singleResult(QAccount.account.balanceBroughtForward);
        if (balanceBF == null) {
            balanceBF = 0.0;
        }
        return balanceBF;
    }

    public Double getBilledInMonth(Long accountId, Long billingMonthId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonthId));
        builder.and(QBill.bill.account.accountId.eq(accountId));

        JPAQuery query = new JPAQuery(entityManager);
        Double totalBilled = query.from(QBill.bill).where(builder).singleResult(QBill.bill.totalBilled);
        if (totalBilled == null)
            totalBilled = 0.0;
        return totalBilled;
    }

    public Long getTotalAccountsBilled(Long billingMonthId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonthId));

        JPAQuery query = new JPAQuery(entityManager);
        Long count = query.from(QBill.bill).where(builder).count();
        if (count == null)
            count = 0l;
        return count;
    }


    public Double getBilledInMonth(Long billingMonthId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonthId));
        JPAQuery query = new JPAQuery(entityManager);
        Double totalBilled = query.from(QBill.bill).where(builder).singleResult(QBill.bill.totalBilled.sum());
        if (totalBilled == null)
            totalBilled = 0.0;
        return totalBilled;
    }

    public Double getTotalBilledAmount(Long accountId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QBill.bill.account.accountId.eq(accountId));
        JPAQuery query = new JPAQuery(entityManager);
        Double totalBilled = query.from(QBill.bill).where(builder).singleResult(QBill.bill.totalBilled.sum());
        if (totalBilled == null)
            totalBilled = 0.0;

        totalBilled = totalBilled + getBalanceBroughtForward(accountId);
        return totalBilled;
    }

    public Double getLastMeterReading(Long accountId) {
        Double meterReading = 0.0;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QBill.bill.account.accountId.eq(accountId));
        JPAQuery query = new JPAQuery(entityManager);
        Long lastBillId = query.from(QBill.bill).where(builder).orderBy(QBill.bill.billCode.desc()).singleResult(QBill.bill.billId.max());
        if (lastBillId != null) {
            builder = new BooleanBuilder();
            builder.and(QBill.bill.billId.eq(lastBillId));
            query = new JPAQuery(entityManager);
            meterReading = query.from(QBill.bill).where(builder).singleResult(QBill.bill.currentReading);
            if (meterReading == null) {
                meterReading = 0.0;
            }
        }
        return meterReading;
    }
}