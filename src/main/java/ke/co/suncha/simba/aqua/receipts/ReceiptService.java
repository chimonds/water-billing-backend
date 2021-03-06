package ke.co.suncha.simba.aqua.receipts;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.aqua.account.QAccount;
import ke.co.suncha.simba.aqua.models.QPayment;
import ke.co.suncha.simba.aqua.services.MeterReadingService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by maitha.manyala on 9/6/17.
 */
@Service
@Transactional
public class ReceiptService {
    @Autowired
    EntityManager entityManager;

    @Autowired
    MeterReadingService meterReadingService;

    public Double getReceiptsToday(List<Long> zoneList) {
        DateTime today = new DateTime().withTimeAtStartOfDay().hourOfDay().withMinimumValue();
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QPayment.payment.transactionDate.goe(today));
        builder.and(QPayment.payment.paymentType.unique.eq(Boolean.TRUE));
        if (!zoneList.isEmpty()) {
            builder.and(QPayment.payment.account.zone.zoneId.in(zoneList));
        }

        JPAQuery query = new JPAQuery(entityManager);
        Double amount = query.from(QPayment.payment).where(builder).singleResult(QPayment.payment.amount.sum());
        if (amount == null) {
            amount = 0.0;
        }
        return amount;
    }

    public Double getReceiptsYesterday(List<Long> zoneList) {
        DateTime yesterdayStartOfDay = new DateTime().withTimeAtStartOfDay().hourOfDay().withMinimumValue().minusDays(1);

        DateTime yesterdayEndOfDay = yesterdayStartOfDay.hourOfDay().withMaximumValue();

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QPayment.payment.transactionDate.goe(yesterdayStartOfDay));
        builder.and(QPayment.payment.transactionDate.loe(yesterdayEndOfDay));
        builder.and(QPayment.payment.paymentType.unique.eq(Boolean.TRUE));
        if (!zoneList.isEmpty()) {
            builder.and(QPayment.payment.account.zone.zoneId.in(zoneList));
        }

        JPAQuery query = new JPAQuery(entityManager);
        Double amount = query.from(QPayment.payment).where(builder).singleResult(QPayment.payment.amount.sum());
        if (amount == null) {
            amount = 0.0;
        }
        return amount;
    }

    public Double getReceiptsThisMonth(List<Long> zoneList) {
        DateTime startOfMonth = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay().hourOfDay().withMinimumValue();
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QPayment.payment.transactionDate.goe(startOfMonth));
        builder.and(QPayment.payment.paymentType.unique.eq(Boolean.TRUE));
        if (!zoneList.isEmpty()) {
            builder.and(QPayment.payment.account.zone.zoneId.in(zoneList));
        }
        JPAQuery query = new JPAQuery(entityManager);
        Double amount = query.from(QPayment.payment).where(builder).singleResult(QPayment.payment.amount.sum());
        if (amount == null) {
            amount = 0.0;
        }
        return amount;
    }


    public Double getReceiptsThisMonthCalculated(List<Long> zoneList) {
        JPAQuery query = new JPAQuery(entityManager);
        BooleanBuilder builder = new BooleanBuilder();
        if (!zoneList.isEmpty()) {
            builder.and(QAccount.account.zone.zoneId.in(zoneList));
        }
        Double amount = query.from(QAccount.account).where(builder).singleResult(QAccount.account.receiptsThisMonthCalculated.sum());
        if (amount == null) {
            amount = 0.0;
        }
        return amount;
    }

    public Double getReceiptsToDate(Long accountId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QPayment.payment.account.accountId.eq(accountId));
        JPAQuery query = new JPAQuery(entityManager);
        Double amount = query.from(QPayment.payment).where(builder).singleResult(QPayment.payment.amount.sum());
        if (amount == null) {
            amount = 0.0;
        }
        return amount;
    }

    public Double getReceiptsBy(Long accountId, DateTime toDate) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QPayment.payment.account.accountId.eq(accountId));
        builder.and(QPayment.payment.transactionDate.loe(toDate));
        JPAQuery query = new JPAQuery(entityManager);
        Double amount = query.from(QPayment.payment).where(builder).singleResult(QPayment.payment.amount.sum());
        if (amount == null) {
            amount = 0.0;
        }
        return amount;
    }

    public Double getReceiptsBetween(Long accountId, DateTime fromDate, DateTime toDate) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QPayment.payment.account.accountId.eq(accountId));
        builder.and(QPayment.payment.transactionDate.goe(fromDate));
        builder.and(QPayment.payment.transactionDate.loe(toDate));
        JPAQuery query = new JPAQuery(entityManager);
        Double amount = query.from(QPayment.payment).where(builder).singleResult(QPayment.payment.amount.sum());
        if (amount == null) {
            amount = 0.0;
        }
        return amount;
    }
}