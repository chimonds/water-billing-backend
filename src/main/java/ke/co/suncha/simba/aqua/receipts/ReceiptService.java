package ke.co.suncha.simba.aqua.receipts;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.aqua.models.QPayment;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

/**
 * Created by maitha.manyala on 9/6/17.
 */
@Service
@Transactional
public class ReceiptService {
    @Autowired
    EntityManager entityManager;

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