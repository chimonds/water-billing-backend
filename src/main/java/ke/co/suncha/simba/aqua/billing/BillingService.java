package ke.co.suncha.simba.aqua.billing;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.account.AccountService;
import ke.co.suncha.simba.aqua.account.BillingFrequency;
import ke.co.suncha.simba.aqua.account.QAccount;
import ke.co.suncha.simba.aqua.models.BillingMonth;
import ke.co.suncha.simba.aqua.repository.BillRepository;
import ke.co.suncha.simba.aqua.services.BillingMonthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

/**
 * Created by maitha.manyala on 9/6/17.
 */
@Service
public class BillingService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    EntityManager entityManager;

    @Autowired
    BillingMonthService billingMonthService;

    @Autowired
    AccountService accountService;

    @Autowired
    BillRepository billRepository;

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

    public Bill getById(Long billId) {
        return billRepository.findOne(billId);
    }

    @Transactional
    public Bill getLastBill(Long accountId) {
        Bill lastBill = new Bill();
        // get current billing month
        BillingMonth billingMonth = billingMonthService.getActiveMonth();

        Account account = accountService.getById(accountId);


        log.info("Getting the most current bill for:" + account.getAccNo());

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QBill.bill.account.accountId.eq(accountId));

        //Check account billing frequency. If account billed montly do not fetch transfered bills
        if (account.getBillingFrequency() == BillingFrequency.MONTHLY) {
            builder.and(QBill.bill.transferred.eq(Boolean.FALSE));
        }

        JPAQuery query = new JPAQuery(entityManager);
        Long lastBillId = query.from(QBill.bill).where(builder).orderBy(QBill.bill.billCode.desc()).singleResult(QBill.bill.billId.max());


        if (lastBillId == null) {
            // seems its initial bill so check if account is metered
            if (account.isMetered()) {
                lastBill.setCurrentReading(account.getMeter().getInitialReading());
                lastBill.setBilled(false);
            } else {
                // TODO;
                lastBill.setCurrentReading(0.0);
                lastBill.setBilled(false);
            }
        } else {

            lastBill = getById(lastBillId);

            log.info("Most current bill:" + lastBill.toString());

            if (account.getMeter() != null) {
                if (account.getMeter().getIsNew() != null) {
                    if (account.getMeter().getIsNew()) {
                        lastBill.setCurrentReading(account.getMeter().getInitialReading());
                    }
                }
            }


            //log.info("Most current bill:" + lastBill.getBillingMonth().getMonth().get(Calendar.YEAR) + "-" + lastBill.getBillingMonth().getMonth().get(Calendar.MONTH));
            log.info("Billing month:" + billingMonth.getMonth().toString("MMM, yyyy"));

            if (lastBill.getBillingMonth().getMonth().isBefore(billingMonth.getMonth())) {
                log.info("Billed:false");
                lastBill.setBilled(Boolean.FALSE);
            } else {
                log.info("Billed:Yes");
                lastBill.setBilled(Boolean.TRUE);
            }

            if (lastBill.getBillingMonth().getMonth().equals(billingMonth.getMonth())) {
                lastBill.setBilled(Boolean.TRUE);
                log.info("Billed:Yes");
            }
//            if (lastBill.isBilled()) {
//                if (lastBill.getTransferred()) {
//                    lastBill.setBilled(Boolean.FALSE);
//                }
//            }
        }

        if (account != null) {
            if (account.getBillingFrequency() == BillingFrequency.ANY_TIME) {
                lastBill.setBilled(Boolean.FALSE);
            }
        }

        return lastBill;
    }

}