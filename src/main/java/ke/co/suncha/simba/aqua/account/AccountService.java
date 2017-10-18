package ke.co.suncha.simba.aqua.account;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPAUpdateClause;
import ke.co.suncha.simba.aqua.billing.BillingServiceImpl;
import ke.co.suncha.simba.aqua.billing.QBill;
import ke.co.suncha.simba.aqua.models.QPayment;
import ke.co.suncha.simba.aqua.receipts.ReceiptService;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.services.BillingMonthService;
import ke.co.suncha.simba.aqua.toActivate.ToActivateService;
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
public class AccountService {
    @Autowired
    EntityManager entityManager;

    @Autowired
    BillingServiceImpl billingService;

    @Autowired
    ReceiptService receiptService;

    @Autowired
    BillingMonthService billingMonthService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ToActivateService toActivateService;

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public Double getTotalBalances(List<Long> zoneList) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QAccount.account.outstandingBalance.gt(0d));
        if (!zoneList.isEmpty()) {
            builder.and(QAccount.account.zone.zoneId.in(zoneList));
        }
        JPAQuery query = new JPAQuery(entityManager);
        Double amount = query.from(QAccount.account).where(builder).singleResult(QAccount.account.outstandingBalance.sum());
        if (amount == null) {
            amount = 0.0d;
        }
        return amount;
    }

    public Long getTotalActiveAccounts(List<Long> zoneList) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QAccount.account.onStatus.eq(1));
        builder.and(QAccount.account.active.eq(Boolean.TRUE));
        if (!zoneList.isEmpty()) {
            builder.and(QAccount.account.zone.zoneId.in(zoneList));
        }
        JPAQuery query = new JPAQuery(entityManager);
        Long count = query.from(QAccount.account).where(builder).singleResult(QAccount.account.accountId.count());
        if (count == null) {
            count = 0L;
        }
        return count;
    }

    public Long getTotalInActiveAccounts(List<Long> zoneList) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QAccount.account.onStatus.eq(1));
        builder.and(QAccount.account.active.eq(Boolean.FALSE));
        if (!zoneList.isEmpty()) {
            builder.and(QAccount.account.zone.zoneId.in(zoneList));
        }
        JPAQuery query = new JPAQuery(entityManager);
        Long count = query.from(QAccount.account).where(builder).singleResult(QAccount.account.accountId.count());
        if (count == null) {
            count = 0L;
        }
        return count;
    }

    public Long getTotalAccounts(List<Long> zoneList) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QAccount.account.onStatus.eq(1));
        if (!zoneList.isEmpty()) {
            builder.and(QAccount.account.zone.zoneId.in(zoneList));
        }
        JPAQuery query = new JPAQuery(entityManager);
        Long count = query.from(QAccount.account).where(builder).singleResult(QAccount.account.accountId.count());
        if (count == null) {
            count = 0L;
        }
        return count;
    }

    public Account getById(Long accountId) {
        return accountRepository.findOne(accountId);
    }

    public Double getTotalCreditBalances(List<Long> zoneList) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QAccount.account.outstandingBalance.lt(0d));
        if (!zoneList.isEmpty()) {
            builder.and(QAccount.account.zone.zoneId.in(zoneList));
        }
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

    @Transactional
    public Account updateBalance(Long accountId) {
        // update balances
        Account account = accountRepository.findOne(accountId);
        try {
            if (!accountRepository.exists(accountId)) {
                return null;
            }

            JPAQuery query = new JPAQuery(entityManager);
            BooleanBuilder accountBuilder = new BooleanBuilder();
            accountBuilder.and(QAccount.account.accountId.eq(accountId));
            Double balanceBroughtForward = query.from(QAccount.account).where(accountBuilder).singleResult(QAccount.account.balanceBroughtForward);
            if (balanceBroughtForward == null) {
                balanceBroughtForward = 0d;
            }

            Double balance = 0d;

            // add balance b/f
            balance += balanceBroughtForward;

            Double waterSaleTotal = 0d;
            waterSaleTotal += balance;

            Double meterRentTotal = 0d;
            Double penaltiesTotal = 0d;

            //get total billed amount for all bills
            //Double dbTotalWaterSaleBilled = billRepository.getTotalBilledAmountByAccount(account.getAccountId());

            query = new JPAQuery(entityManager);
            BooleanBuilder billsBuilder = new BooleanBuilder();
            billsBuilder.and(QBill.bill.account.accountId.eq(accountId));
            Double dbTotalWaterSaleBilled = query.from(QBill.bill).where(billsBuilder).singleResult(QBill.bill.amount.sum());


            if (dbTotalWaterSaleBilled != null) {
                balance += dbTotalWaterSaleBilled;
                waterSaleTotal += dbTotalWaterSaleBilled;
            }

            //get total meter rent for all bills
            //Double dbTotalMeterRentBilled = billRepository.getTotalMeterRentByAccount(account.getAccountId());
            query = new JPAQuery(entityManager);
            Double dbTotalMeterRentBilled = query.from(QBill.bill).where(billsBuilder).singleResult(QBill.bill.meterRent.sum());

            if (dbTotalMeterRentBilled != null) {
                balance += dbTotalMeterRentBilled;
                meterRentTotal += dbTotalMeterRentBilled;
            }

            //Get total_billed-meter_rent-amount
            query = new JPAQuery(entityManager);
            Double dbTotalFines = query.from(QBill.bill).where(billsBuilder).singleResult(QBill.bill.totalBilled.sum().subtract(QBill.bill.meterRent.sum().add(QBill.bill.amount.sum())));


            //Double dbTotalFines = billRepository.getTotalFinesByAccount(account.getAccountId());
            if (dbTotalFines != null) {
                balance += dbTotalFines;
                penaltiesTotal += dbTotalFines;
            }


            Double totalPayments = 0d;
            query = new JPAQuery(entityManager);
            BooleanBuilder paymentBuilder = new BooleanBuilder();
            paymentBuilder.and(QPayment.payment.account.accountId.eq(accountId));
            Double dbTotalPayments = query.from(QPayment.payment).where(paymentBuilder).singleResult(QPayment.payment.amount.sum());


            //Double dbTotalPayments = paymentRepository.getTotalByAccount(account.getAccountId());
            if (dbTotalPayments != null) {
                totalPayments += dbTotalPayments;
            }

            balance = (balance - totalPayments);


            account.setOutstandingBalance(balance);

            Double moneyToAllocate = 0d;
            //if balance
            if (balance == 0) {
                account.setOutstandingBalance(0d);
                account.setPenaltiesBalance(0d);
                account.setWaterSaleBalance(0d);
                account.setMeterRentBalance(0d);
            } else if (balance > 0) {
                account.setPenaltiesBalance(penaltiesTotal);
                account.setWaterSaleBalance(waterSaleTotal);
                account.setMeterRentBalance(meterRentTotal);

                //Penalties
                moneyToAllocate = totalPayments - penaltiesTotal;
                if (moneyToAllocate >= 0) {
                    account.setPenaltiesBalance(0d);
                } else {
                    account.setPenaltiesBalance(Math.abs(moneyToAllocate));
                }

                //Meter Rent
                if (moneyToAllocate > 0) {
                    moneyToAllocate = moneyToAllocate - meterRentTotal;
                    if (moneyToAllocate >= 0) {
                        account.setMeterRentBalance(0d);
                    } else {
                        account.setMeterRentBalance(Math.abs(moneyToAllocate));
                    }
                }

                //Water Sale
                if (moneyToAllocate > 0) {
                    moneyToAllocate = moneyToAllocate - waterSaleTotal;
                    if (moneyToAllocate >= 0) {
                        account.setWaterSaleBalance(0d);
                    } else {
                        account.setWaterSaleBalance(Math.abs(moneyToAllocate));
                    }
                }
            }
            account = accountRepository.save(account);


            updateAmountToAllocateThisMonth(accountId);

            //Check if to move account to the list to activate

            toActivateService.create(account.getAccountId());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return account;
    }
}


