package ke.co.suncha.simba.aqua.billing;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.account.AccountService;
import ke.co.suncha.simba.aqua.account.BillingFrequency;
import ke.co.suncha.simba.aqua.account.QAccount;
import ke.co.suncha.simba.aqua.models.BillItem;
import ke.co.suncha.simba.aqua.models.BillItemType;
import ke.co.suncha.simba.aqua.models.BillingMonth;
import ke.co.suncha.simba.aqua.models.Meter;
import ke.co.suncha.simba.aqua.repository.BillItemRepository;
import ke.co.suncha.simba.aqua.repository.BillRepository;
import ke.co.suncha.simba.aqua.services.BillingMonthService;
import ke.co.suncha.simba.aqua.services.MeterService;
import ke.co.suncha.simba.aqua.services.SMSService;
import ke.co.suncha.simba.aqua.services.TariffService;
import ke.co.suncha.simba.aqua.utils.BillMeta;
import ke.co.suncha.simba.aqua.utils.BillRequest;
import ke.co.suncha.simba.aqua.utils.SMSNotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maitha.manyala on 9/6/17.
 */
@Service
public class BillingServiceImpl implements BillingService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    EntityManager entityManager;

    @Autowired
    BillingMonthService billingMonthService;

    @Autowired
    AccountService accountService;

    @Autowired
    BillRepository billRepository;

    @Autowired
    SimbaOptionService optionService;

    @Autowired
    TariffService tariffService;

    @Autowired
    MeterService meterService;

    @Autowired
    BillItemRepository billItemRepository;

    @Autowired
    SMSService smsService;

    @Override
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

    @Override
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

    @Override
    public Long getTotalAccountsBilled(List<Long> zoneList, Long billingMonthId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonthId));
        if (!zoneList.isEmpty()) {
            builder.and(QBill.bill.account.zone.zoneId.in(zoneList));
        }
        JPAQuery query = new JPAQuery(entityManager);
        Long count = query.from(QBill.bill).where(builder).count();
        if (count == null)
            count = 0L;
        return count;
    }

    @Override
    public Double getBilledInMonth(List<Long> zoneList, Long billingMonthId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QBill.bill.billingMonth.billingMonthId.eq(billingMonthId));
        if (!zoneList.isEmpty()) {
            builder.and(QBill.bill.account.zone.zoneId.in(zoneList));
        }
        JPAQuery query = new JPAQuery(entityManager);
        Double totalBilled = query.from(QBill.bill).where(builder).singleResult(QBill.bill.totalBilled.sum());
        if (totalBilled == null)
            totalBilled = 0.0;
        return totalBilled;
    }

    @Override
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

    @Override
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

    @Override
    public Bill getById(Long billId) {
        return billRepository.findOne(billId);
    }

    @Override
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

    @Override
    public Integer getMinimumAverageUnitsToBill() {
        Integer units = 0;
        try {
            units = Integer.parseInt(optionService.getOption("BILL_ON_AVERAGE_UNITS").getValue());
        } catch (Exception ex) {

        }
        if (units == null)
            units = 0;
        return units;
    }

    private String getFormattedNumber(Double val) {
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(val);
    }

    @Override
    public Bill create(BillRequest billRequest, Long accountId) {

        Account account = accountService.getById(accountId);
        Bill bill = new Bill();

        if (billRequest.getTransactionDate() != null) {
            bill.setTransactionDate(billRequest.getTransactionDate());
        }

        //Bill lastBill = getLastBill(accountId);

        bill.setCurrentReading(billRequest.getCurrentReading());
        bill.setPreviousReading(billRequest.getPreviousReading());

        Double unitsConsumed = bill.getCurrentReading() - bill.getPreviousReading();

        unitsConsumed = Double.valueOf(getFormattedNumber(unitsConsumed));

        Integer billOnAverageUnits = getMinimumAverageUnitsToBill();

        if (unitsConsumed > billOnAverageUnits) {
            bill.setConsumptionType("Actual");
        } else {
            if (billRequest.getBillWaterSale()) {
                unitsConsumed = account.getAverageConsumption();
            }
            bill.setConsumptionType("Average");
        }

        //set units consumed
        bill.setUnitsBilled(unitsConsumed);
        bill.setBillWaterSale(billRequest.getBillWaterSale());

        //set meter rent
        if (account.isMetered()) {
            if (account.getMeter().getMeterOwner().getCharge()) {
                bill.setMeterRent(account.getMeter().getMeterSize().getRentAmount());
            }

            Meter dbMeter = meterService.getByMeterId(account.getMeter().getMeterId());
            if (dbMeter != null) {
                meterService.activate(dbMeter.getMeterId());
            }
        }
        //set billing amount
        BillMeta billMeta = new BillMeta();
        billMeta.setUnits(unitsConsumed);
        billMeta = tariffService.calculate(billMeta, accountId);

        bill.setAmount(billMeta.getAmount());
        bill.setContent(billMeta.getContent());

        BillingMonth activeBillingMonth = billingMonthService.getActiveMonth();

        bill.setBillingMonth(activeBillingMonth);
        bill.setBillCode(activeBillingMonth.getCode());
        bill.setAverageConsumption(account.getAverageConsumption());
        bill.setAccount(account);
        bill.setParentAccount(account);
        bill.setTransferred(Boolean.FALSE);
        Bill createdBill = billRepository.save(bill);

        if (billRequest.getBillItemTypes() != null) {
            if (!billRequest.getBillItemTypes().isEmpty()) {
                List<BillItem> billItems = new ArrayList<>();
                for (BillItemType bit : billRequest.getBillItemTypes()) {
                    log.info("Bill item type:" + bit.getName());
                    BillItem bi = new BillItem();
                    bi.setAmount(bit.getAmount());
                    bi.setBill(createdBill);
                    bi.setBillItemType(bit);

                    BillItem createdBillItem = billItemRepository.save(bi);
                    billItems.add(createdBillItem);
                }
                createdBill.setBillItems(billItems);
            }
        }

        Double totalAmount = 0.0;
        if (account.isMetered()) {
            log.info("Applying meter rent...");
            if (account.getMeter().getMeterOwner().getCharge()) {
                totalAmount += account.getMeter().getMeterSize().getRentAmount();
            }
        }

        log.info("Getting total billed...");
        Boolean accountIsActive = true;
        totalAmount += createdBill.getAmount();
        if (createdBill.getBillItems() != null) {
            if (!createdBill.getBillItems().isEmpty()) {
                for (BillItem bi : createdBill.getBillItems()) {
                    totalAmount += bi.getAmount();
                    if (!bi.getBillItemType().isActive()) {
                        accountIsActive = false;
                    }
                }
            }
        }

        createdBill.setTotalBilled(totalAmount);
        createdBill = billRepository.save(bill);


        account.setActive(accountIsActive);
        account.setUpdateBalance(Boolean.TRUE);
        account = accountService.save(account);

        accountService.updateBalance(account.getAccountId());

        //send sms
        if (account.getBillingFrequency() == BillingFrequency.MONTHLY) {
            log.info("Saving SMS notification...");
            smsService.saveNotification(account.getAccountId(), 0L, createdBill.getBillId(), SMSNotificationType.BILL);
        }

        log.info("Account billed successfully:" + account.getAccNo());

        return createdBill;
    }
}