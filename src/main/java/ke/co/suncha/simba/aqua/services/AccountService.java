/*
 * The MIT License
 *
 * Copyright 2015 Maitha Manyala.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ke.co.suncha.simba.aqua.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.account.OnStatus;
import ke.co.suncha.simba.aqua.account.QAccount;
import ke.co.suncha.simba.aqua.scheme.Scheme;
import ke.co.suncha.simba.aqua.scheme.SchemeRepository;
import ke.co.suncha.simba.aqua.billing.Bill;
import ke.co.suncha.simba.aqua.billing.BillService;
import ke.co.suncha.simba.aqua.billing.QBill;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.reports.*;
import ke.co.suncha.simba.aqua.reports.scheduled.ReportHeader;
import ke.co.suncha.simba.aqua.repository.*;
import ke.co.suncha.simba.aqua.scheme.zone.ZoneRepository;
import ke.co.suncha.simba.aqua.toActivate.ToActivateService;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.*;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Service
@Transactional
public class AccountService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private SimbaOptionService optionService;

    @Autowired
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    AuditService auditService;

    @Autowired
    BillRepository billRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    AgeingRecordRepository ageingRecordRepository;

    @Autowired
    AccountStatusHistoryRepository accountStatusHistoryRepository;

    @Autowired
    MbassadorService mbassadorService;

    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    BillService billService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    AgeingDataRepository ageingDataRepository;

    @Autowired
    AccountsUpdateRepository accountsUpdateRepository;

    @Autowired
    SchemeRepository schemeRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    ToActivateService toActivateService;


    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public AccountService() {
    }


    public List<StatementRecord> getStatementRecords(Long accountId) {

        Account account = accountRepository.findOne(accountId);

        //fill report
        List<StatementRecord> records = new ArrayList<StatementRecord>();

        //balance brought forward
        StatementRecord balanceBf = new StatementRecord();
        balanceBf.setTransactionDate(account.getCreatedOn());
        balanceBf.setItemType("Balance B/f");
        balanceBf.setRefNo("");
        balanceBf.setAmount(account.getBalanceBroughtForward());
        records.add(balanceBf);

        //add bills
        if (!account.getBills().isEmpty()) {
            for (Bill bill : account.getBills()) {
                //add bill record
                DateTime billingMonth = bill.getBillingMonth().getMonth();
                String formattedDate = billingMonth.toString("MMM, yyyy");
                //format1.format(billingMonth.getTime());

                StatementRecord billRecord = new StatementRecord();
                //billRecord.setTransactionDate(bill.getTransactionDate());
                billRecord.setTransactionDate(bill.getBillingMonth().getMonth());
                billRecord.setItemType("Bill");
                billRecord.setRefNo(formattedDate);
                billRecord.setAmount(bill.getAmount());
                records.add(billRecord);

                //get billing items
                if (!bill.getBillItems().isEmpty()) {
                    for (BillItem billItem : bill.getBillItems()) {
                        StatementRecord billItemRecord = new StatementRecord();
                        billItemRecord.setTransactionDate(bill.getTransactionDate());
                        billItemRecord.setItemType("Charge");
                        billItemRecord.setRefNo(formattedDate);
                        billItemRecord.setAmount(billItem.getAmount());
                        records.add(billItemRecord);
                    }
                }

                //get meter rent
                if (bill.getMeterRent() > 0) {
                    billRecord = new StatementRecord();
                    billRecord.setTransactionDate(bill.getTransactionDate());
                    billRecord.setItemType("Meter Rent");
                    billRecord.setRefNo(formattedDate);
                    billRecord.setAmount(bill.getMeterRent());
                    records.add(billRecord);
                }
            }
        }

        //add payments
        if (!account.getPayments().isEmpty()) {
            for (Payment payment : account.getPayments()) {
                StatementRecord paymentRecord = new StatementRecord();
                paymentRecord.setTransactionDate(payment.getTransactionDate());

                paymentRecord.setRefNo(payment.getReceiptNo() + "-" + payment.getPaymentType().getName());


                //Double amount = Math.abs(payment.getAmount()) * -1;
                Double amount = payment.getAmount();


                if (payment.getPaymentType().isNegative()) {
                    amount = Math.abs(payment.getAmount());
                } else {
                    if (amount > 0) {
                        amount = Math.abs(payment.getAmount()) * -1;
                    } else {
                        amount = Math.abs(payment.getAmount());
                    }
                }

                paymentRecord.setAmount(amount);
                if (payment.getPaymentType().hasComments()) {
                    paymentRecord.setItemType("Adjustment");
                } else {
                    paymentRecord.setItemType("Payment");
                }
                records.add(paymentRecord);
            }
        }

        if (!records.isEmpty()) {
            //Sort collection by transaction date
            Collections.sort(records);

            Double runningTotal = 0.0;
            //calculate running totals
            Integer location = 0;
            for (StatementRecord record : records) {
                runningTotal += record.getAmount();
                record.setRunningAmount(runningTotal);
                records.set(location, record);
                location++;
            }
        }

        return records;
    }

    public Account getByAccountId(Long accountId) {
        return accountRepository.findOne(accountId);
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

            //Check if to move account to the list to activate

            toActivateService.create(account.getAccountId());
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
        return account;
    }

    @Transactional
    public void updateAccountAgeingRemove(String accNo) {

        AgeingRecord ageingRecord = ageingRecordRepository.findByAccNo(accNo);
        if (ageingRecord == null) {
            ageingRecord = ageingRecord = new AgeingRecord();
        }

        Account acc = accountRepository.findByaccNo(accNo);

        try {
            Long consumerId = accountRepository.findConsumerIdByAccountId(acc.getAccountId());
            if (consumerId != null) {
                Consumer consumer = consumerRepository.findOne(consumerId);
                String fullName = "";
                if (consumer.getFirstName() != null) {
                    fullName += consumer.getFirstName().toUpperCase() + " ";
                }
                if (consumer.getMiddleName() != null) {
                    fullName += consumer.getMiddleName().toUpperCase() + " ";
                }
                if (consumer.getLastName() != null) {
                    fullName += consumer.getLastName().toUpperCase() + " ";
                }
                ageingRecord.setName(fullName);
            }
        } catch (Exception ex) {
            log.info(ex.getMessage());
        }
        ageingRecord.setAccNo(acc.getAccNo());

        //set
        DateTime today = new DateTime();
        today = today.withDayOfMonth(24);

        DateTime above180 = new DateTime();
        above180 = above180.withDayOfMonth(24);
        above180 = above180.minusMonths(6);
        above180 = above180.withDayOfMonth(23);

        DateTime above120 = new DateTime();
        above120 = above120.withDayOfMonth(24);
        above120 = above120.minusMonths(4);
        above120 = above120.withDayOfMonth(23);

        DateTime above90 = new DateTime();
        above90 = above90.withDayOfMonth(24);
        above90 = above90.minusMonths(3);
        above90 = above90.withDayOfMonth(23);

        DateTime above60 = new DateTime();
        above60 = above60.withDayOfMonth(24);
        above60 = above60.minusMonths(2);
        above60 = above60.withDayOfMonth(23);

        DateTime above30 = new DateTime();
        above30 = above30.withDayOfMonth(24);
        above30 = above30.minusMonths(1);
        above30 = above30.withDayOfMonth(23);

        //get all payments to date
        Double allocationBalance = 0d;
        Double total_payments = paymentService.getAccountTotalPayments(acc.getAccountId());
        Double BILLS_NOT_PAID = 0d;

        //Start Bills above 180 days
        //Double billsAbove180Days = billService.getAccountBillsByDate(acc.getAccountId(), above180);
        Double billAbove180Days = billService.getAccountBillsByDateWithBalanceBF(acc.getAccountId(), above180);
        Double balance180days = 0d;
        BILLS_NOT_PAID = billAbove180Days;

        Double BILL_ABOVE_180_DAYS = billAbove180Days;
        Double balance_not_paid_above = BILL_ABOVE_180_DAYS;

        //if payments are greater or == to bills
        if (total_payments >= billAbove180Days) {
            BILLS_NOT_PAID = 0d;
            balance180days = 0d;//all bills have been paid
            total_payments = total_payments - BILL_ABOVE_180_DAYS;
        }
        //payments less than bills but not zero
        else if (total_payments < billAbove180Days && total_payments > 0) {
            BILLS_NOT_PAID = BILL_ABOVE_180_DAYS - total_payments;
            balance180days = BILL_ABOVE_180_DAYS - total_payments;//all bills have been paid
            //money finished
            total_payments = 0d;
        }
        //no payment done
        else if (total_payments == 0d) {
            BILLS_NOT_PAID = billAbove180Days;
            balance180days = billAbove180Days;
            total_payments = 0d;
        }

        if (balance_not_paid_above > 0) {

        }
        //Start Bills above 180 days

        //Start Bills above 120-180 days
        Double billAbove120Days = billService.getAccountBillsByDateWithBalanceBF(acc.getAccountId(), above120);
        Double balance120days = 0d;
        Double BILL_ABOVE_120_DAYS = BILLS_NOT_PAID;
        Double bills_not_paid_above_180_days = BILLS_NOT_PAID;

        if (billAbove120Days > billAbove180Days) {
            BILL_ABOVE_120_DAYS = (billAbove120Days - billAbove180Days) + BILLS_NOT_PAID;
            //BILL_ABOVE_120_DAYS = (billAbove120Days - billAbove180Days);
            balance120days = BILL_ABOVE_120_DAYS;
        }


        //if payments are greater or == to bills
        if (total_payments >= BILL_ABOVE_120_DAYS) {
            BILLS_NOT_PAID = 0d;
            //all bills have been paid
            balance120days = 0d;
            total_payments = total_payments - BILL_ABOVE_120_DAYS;
        }
        //payments less than bills but not zero
        else if (total_payments < BILL_ABOVE_120_DAYS && total_payments > 0) {
            BILLS_NOT_PAID = BILL_ABOVE_120_DAYS - total_payments;
            balance120days = BILL_ABOVE_120_DAYS - total_payments;//all bills have been paid
            //money finished
            total_payments = 0d;
        }
        //no payment done
        else if (total_payments == 0d) {
            BILLS_NOT_PAID = BILL_ABOVE_120_DAYS;// billAbove120Days;
            total_payments = 0d;
        }


        if (bills_not_paid_above_180_days > 0 && balance120days > 0) {
            balance120days = balance120days - bills_not_paid_above_180_days;
        }


        //End Bills above 120-180 days

        //Start Bills above 90-120 days
        Double billAbove90Days = billService.getAccountBillsByDateWithBalanceBF(acc.getAccountId(), above90);
        Double balance90days = 0d;

        Double BILL_ABOVE_90_DAYS = BILLS_NOT_PAID;
        Double bills_not_paid_above_90_days = BILLS_NOT_PAID;

        if (billAbove90Days > billAbove120Days) {
            BILL_ABOVE_90_DAYS = (billAbove90Days - billAbove120Days) + BILLS_NOT_PAID;
            balance90days = BILL_ABOVE_90_DAYS;
        }

        //if payments are greater or == to bills
        if (total_payments >= BILL_ABOVE_90_DAYS) {
            BILLS_NOT_PAID = 0d;
            //all bills have been paid
            balance90days = 0d;
            total_payments = total_payments - BILL_ABOVE_90_DAYS;
        }
        //payments less than bills but not zero
        else if (total_payments < BILL_ABOVE_90_DAYS && total_payments > 0) {
            BILLS_NOT_PAID = BILL_ABOVE_90_DAYS - total_payments;
            balance90days = BILL_ABOVE_90_DAYS - total_payments;//all bills have been paid
            //money finished
            total_payments = 0d;
        }
        //no payment done
        else if (total_payments == 0d) {
            BILLS_NOT_PAID = BILL_ABOVE_90_DAYS;// billAbove90Days;
            total_payments = 0d;
        }

        if (bills_not_paid_above_90_days > 0 && balance90days > 0) {
            balance90days = balance90days - bills_not_paid_above_90_days;
        }
        //End Bills above 90-120 days

        //Start Bills above 60 days
        Double billAbove60Days = billService.getAccountBillsByDateWithBalanceBF(acc.getAccountId(), above60);
        Double balance60days = 0d;

        Double BILL_ABOVE_60_DAYS = BILLS_NOT_PAID;
        Double bills_not_paid_above_60_days = BILLS_NOT_PAID;

        if (billAbove60Days > billAbove90Days) {
            BILL_ABOVE_60_DAYS = (billAbove60Days - billAbove90Days) + BILLS_NOT_PAID;
            balance60days = BILL_ABOVE_60_DAYS;
        }

        //if payments are greater or == to bills
        if (total_payments >= BILL_ABOVE_60_DAYS) {
            BILLS_NOT_PAID = 0d;
            //billAbove60Days = 0;//all bills have been paid
            balance60days = 0d;
            total_payments = total_payments - BILL_ABOVE_60_DAYS;
        }
        //payments less than bills but not zero
        else if (total_payments < BILL_ABOVE_60_DAYS && total_payments > 0) {
            BILLS_NOT_PAID = BILL_ABOVE_60_DAYS - total_payments;
            balance60days = BILL_ABOVE_60_DAYS - total_payments;//all bills have been paid
            //money finished
            total_payments = 0d;
        }
        //no payment done
        else if (total_payments == 0) {
            BILLS_NOT_PAID = BILL_ABOVE_60_DAYS;// billAbove60Days;
            total_payments = 0d;
        }

        if (bills_not_paid_above_60_days > 0 && balance60days > 0) {
            balance60days = balance60days - bills_not_paid_above_60_days;
        }
        //End Bills above 60 days

        //Start Bills above 30 days
        Double billAbove30Days = billService.getAccountBillsByDateWithBalanceBF(acc.getAccountId(), above30);
        Double balance30days = 0d;

        Double BILL_ABOVE_30_DAYS = BILLS_NOT_PAID;
        Double bills_not_above_30_days = BILLS_NOT_PAID;

        if (billAbove30Days > billAbove60Days) {
            BILL_ABOVE_30_DAYS = (billAbove30Days - billAbove60Days) + BILLS_NOT_PAID;
            balance30days = BILL_ABOVE_30_DAYS;
        }


        //if payments are greater or == to bills
        if (total_payments >= BILL_ABOVE_30_DAYS) {
            BILLS_NOT_PAID = 0d;
            //billAbove30Days = 0;//all bills have been paid
            balance30days = 0d;
            total_payments = total_payments - BILL_ABOVE_30_DAYS;
        }
        //payments less than bills but not zero
        else if (total_payments < BILL_ABOVE_30_DAYS && total_payments > 0) {
            BILLS_NOT_PAID = BILL_ABOVE_30_DAYS - total_payments;
            balance30days = BILL_ABOVE_30_DAYS - total_payments;//all bills have been paid
            //money finished
            total_payments = 0d;
        }
        //no payment done
        else if (total_payments == 0) {
            BILLS_NOT_PAID = BILL_ABOVE_30_DAYS;
            total_payments = 0d;
        }
        if (bills_not_above_30_days > 0 && balance30days > 0) {
            balance30days = balance30days - bills_not_above_30_days;
        }
        //End Bills above 30 days

        //Start Bills above 0 days
        Double billAbove0Days = billService.getAccountBillsByDateWithBalanceBF(acc.getAccountId(), today);
        Double balance0days = 0d;

        Double BILL_ABOVE_0_DAYS = BILLS_NOT_PAID;
        Double bills_not_paid_above_0_days = BILLS_NOT_PAID;

        if (billAbove0Days > billAbove30Days) {
            BILL_ABOVE_0_DAYS = (billAbove0Days - billAbove30Days) + BILLS_NOT_PAID;
            balance0days = BILL_ABOVE_0_DAYS;
        } else {
            BILL_ABOVE_0_DAYS = (billAbove0Days - billAbove30Days) + BILLS_NOT_PAID;
            balance0days = BILL_ABOVE_0_DAYS;
        }

        //if payments are greater or == to bills
        if (total_payments >= BILL_ABOVE_0_DAYS) {
            BILLS_NOT_PAID = 0d;
            //billAbove0Days = 0;//all bills have been paid
            balance0days = 0d;
            total_payments = total_payments - BILL_ABOVE_0_DAYS;
        }
        //payments less than bills but not zero
        else if (total_payments < BILL_ABOVE_0_DAYS && total_payments > 0) {
            BILLS_NOT_PAID = BILL_ABOVE_0_DAYS - total_payments;
            balance0days = BILL_ABOVE_0_DAYS - total_payments;//all bills have been paid
            //money finished
            total_payments = 0d;
        }
        //no payment done
        else if (total_payments == 0) {
            BILLS_NOT_PAID = BILL_ABOVE_0_DAYS;// billAbove0Days;
            total_payments = 0d;
        }

        if (bills_not_paid_above_0_days > 0 && balance0days > 0) {
            balance0days = balance0days - bills_not_paid_above_0_days;
        }
        //End Bills above 0 days


        Account account = accountRepository.findOne(acc.getAccountId());
        try {
            if (account.getConsumer() != null) {
                String fullName = "";
                //log.info(account.getConsumer().getConsumerId() + "");

                if (account.getConsumer().getFirstName() != null) {
                    fullName = account.getConsumer().getFirstName();
                }

                if (account.getConsumer().getMiddleName() != null) {
                    fullName = fullName + " " + account.getConsumer().getMiddleName();
                }

                if (account.getConsumer().getLastName() != null) {
                    fullName = fullName + " " + account.getConsumer().getLastName();
                }
                ageingRecord.setName(fullName);
            }
        } catch (Exception ex) {

        }

        if (account.getZone() != null) {
            ageingRecord.setZone(account.getZone().getName());
        }


        ageingRecord.setAbove0(acc.getOutstandingBalance());
        ageingRecord.setAbove30(balance30days);
        ageingRecord.setAbove60(balance60days);
        ageingRecord.setAbove90(balance90days);
        ageingRecord.setAbove120(balance120days);
        ageingRecord.setAbove180(balance180days);
        ageingRecord.setAccount(acc);
        ageingRecord.setAccNo(acc.getAccNo());
        if (acc.isActive()) {
            ageingRecord.setCutOff("Active");
        } else {
            ageingRecord.setCutOff("Inactive");
        }

        ageingRecord.setBalance(getAccountBalance(acc.getAccountId()));
        //save

        ageingRecordRepository.save(ageingRecord);
    }

    @Transactional
    private Double getReceiptsByDate(Long accountId, DateTime toDate) {
        Double amount = 0d;
        JPAQuery query = new JPAQuery(entityManager);
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QPayment.payment.account.accountId.eq(accountId));

        //SQLExpressions.date did not match expected type
        //builder.and(SQLExpressions.date(QPayment.payment.transactionDate).loe(date));
        builder.and(QPayment.payment.transactionDate.loe(toDate));
        Double dbAmount = query.from(QPayment.payment).where(builder).singleResult(QPayment.payment.amount.sum());
        if (dbAmount != null) {
            amount += dbAmount;
        }
        return amount;
    }

    @Transactional
    private Double getBillsByDate(Long accountId, DateTime toDate) {
        Double amount = 0d;
        JPAQuery query = new JPAQuery(entityManager);
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QBill.bill.account.accountId.eq(accountId));
        //sixMonthsBuilder.and(QBill.bill.transactionDate.loe(sixMonthsAgoC));
        builder.and(QBill.bill.transactionDate.loe(toDate));
        Double dbAmount = query.from(QBill.bill).where(builder).singleResult(QBill.bill.totalBilled.sum());
        if (dbAmount != null) {
            amount += dbAmount;
        }
        return amount;
    }

    @Transactional
    public void updateAccountAgeingCustom(Long accountId, DateTime toDate, ReportHeader reportHeader) {
        Double balanceBroughtForward = 0d;
        String accNo = "";
        String accountStatus = "Active";
        String zone = "Not Available";
        String consumerName = "";

        JPAQuery query = new JPAQuery(entityManager);
        BooleanBuilder accountBuilder = new BooleanBuilder();
        accountBuilder.and(QAccount.account.accountId.eq(accountId));
        List<Tuple> tuples = query.from(QAccount.account).where(accountBuilder).list(QAccount.account.balanceBroughtForward, QAccount.account.accNo, QAccount.account.active, QAccount.account.zone.name, QAccount.account.consumer.firstName, QAccount.account.consumer.middleName, QAccount.account.consumer.lastName);
        if (!tuples.isEmpty()) {
            Tuple tuple = tuples.get(0);
            Double dbBalanceBroughtForward = tuple.get(QAccount.account.balanceBroughtForward);
            if (dbBalanceBroughtForward != null) {
                balanceBroughtForward += dbBalanceBroughtForward;
            }

            //account number
            accNo = tuple.get(QAccount.account.accNo);

            //Account status
            Boolean active = tuple.get(QAccount.account.active);
            if (!active) {
                accountStatus = "Inactive";
            }

            //Zone
            zone = tuple.get(QAccount.account.zone.name);

            //consumer name
            consumerName = tuple.get(QAccount.account.consumer.firstName) + " " + tuple.get(QAccount.account.consumer.middleName) + " " + tuple.get(QAccount.account.consumer.lastName);
            consumerName = consumerName.replace("null", "");
        }


        //DateTime today = toDate.withMonthOfYear(6).dayOfMonth().withMaximumValue().hourOfDay().withMaximumValue();
        DateTime today = toDate;
        DateTime sixMonthsAgo = today.minusMonths(6);
        DateTime fourMonthsAgo = today.minusMonths(4);
        DateTime threeMonthsAgo = today.minusMonths(3);
        DateTime twoMonthsAgo = today.minusMonths(2);
        DateTime oneMonthAgo = today.minusMonths(1);
        Double billsNotPaid = 0d;

        //Balances
        Double balanceSixMonthsAgo = 0d;
        Double balanceFourMonthsAgo = 0d;
        Double balanceThreeMonthsAgo = 0d;
        Double balanceTwoMonthsAgo = 0d;
        Double balanceOneMonthsAgo = 0d;
        Double balanceToday = 0d;

        //Get all payments unit today (Today is supplied by the user)
        //TODO;
        Double totalPayments = getReceiptsByDate(accountId, today);


        //get bills above 180 days
        //Double billsSixMonthsAgo = billService.getAccountBillsByDate(accountId, sixMonthsAgo) + balanceBroughtForward;
        Double billsSixMonthsAgo = this.getBillsByDate(accountId, sixMonthsAgo) + balanceBroughtForward;
        Double billsSixMonthsAgoX = billsSixMonthsAgo;

        if (totalPayments >= billsSixMonthsAgo) {
            billsNotPaid = 0d;
            balanceSixMonthsAgo = 0d;
            totalPayments = totalPayments - billsSixMonthsAgo;
        }
        //payments less than bills but not zero
        else if (totalPayments < billsSixMonthsAgo && totalPayments > 0) {
            billsNotPaid = billsSixMonthsAgo - totalPayments;
            balanceSixMonthsAgo = billsSixMonthsAgo - totalPayments;
            totalPayments = 0d;
        }
        //No payments done
        else {
            //Nothing paid
            billsNotPaid = billsSixMonthsAgo;
            balanceSixMonthsAgo = billsSixMonthsAgo;
            totalPayments = 0d;
        }

        //Four months ago
        Double billsFourMonthsAgo = getBillsByDate(accountId, fourMonthsAgo) + balanceBroughtForward;
        Double billsFourMonthsAgoX = billsFourMonthsAgo;

        if (billsFourMonthsAgo > billsSixMonthsAgoX) {
            billsFourMonthsAgo = billsFourMonthsAgo - billsSixMonthsAgoX;
            balanceFourMonthsAgo = billsNotPaid;

            if (totalPayments >= billsFourMonthsAgo) {
                billsNotPaid = 0d;
                balanceFourMonthsAgo = 0d;
                totalPayments = totalPayments - billsFourMonthsAgo;
            } else if (totalPayments < billsFourMonthsAgo && totalPayments > 0) {
                billsNotPaid = billsFourMonthsAgo - totalPayments;
                balanceFourMonthsAgo = billsFourMonthsAgo - totalPayments;
                totalPayments = 0d;
            } else {
                //Nothing paid
                billsNotPaid = billsFourMonthsAgo;
                balanceFourMonthsAgo = billsFourMonthsAgo;
                totalPayments = 0d;
            }
        }


        //Three Months
        Double billsThreeMonthsAgo = getBillsByDate(accountId, threeMonthsAgo) + balanceBroughtForward;
        Double billsThreeMonthsAgoX = billsThreeMonthsAgo;

        if (billsThreeMonthsAgo > billsFourMonthsAgoX) {
            billsThreeMonthsAgo = billsThreeMonthsAgo - billsFourMonthsAgoX;
            balanceThreeMonthsAgo = billsNotPaid;

            if (totalPayments >= billsThreeMonthsAgo) {
                billsNotPaid = 0d;
                balanceThreeMonthsAgo = 0d;
                totalPayments = totalPayments - billsThreeMonthsAgo;
            } else if (totalPayments < billsThreeMonthsAgo && totalPayments > 0) {
                billsNotPaid = billsThreeMonthsAgo - totalPayments;
                balanceThreeMonthsAgo = billsThreeMonthsAgo - totalPayments;
                totalPayments = 0d;
            } else {
                //Nothing paid
                billsNotPaid = billsThreeMonthsAgo;
                balanceThreeMonthsAgo = billsThreeMonthsAgo;
                totalPayments = 0d;
            }
        }


        //Two Months
        Double billsTwoMonthsAgo = getBillsByDate(accountId, twoMonthsAgo) + balanceBroughtForward;
        Double billsTwoMonthsAgoX = billsTwoMonthsAgo;

        if (billsTwoMonthsAgo > billsThreeMonthsAgoX) {
            billsTwoMonthsAgo = billsTwoMonthsAgo - billsThreeMonthsAgoX;
            balanceTwoMonthsAgo = billsNotPaid;

            if (totalPayments >= billsTwoMonthsAgo) {
                billsNotPaid = 0d;
                balanceTwoMonthsAgo = 0d;
                totalPayments = totalPayments - billsTwoMonthsAgo;
            } else if (totalPayments < billsTwoMonthsAgo && totalPayments > 0) {
                billsNotPaid = billsTwoMonthsAgo - totalPayments;
                balanceTwoMonthsAgo = billsTwoMonthsAgo - totalPayments;
                totalPayments = 0d;
            } else {
                //Nothing paid
                billsNotPaid = billsTwoMonthsAgo;
                balanceTwoMonthsAgo = billsTwoMonthsAgo;
                totalPayments = 0d;
            }
        }


        //One Month
        Double billsOneMonthAgo = getBillsByDate(accountId, oneMonthAgo) + balanceBroughtForward;
        Double billsOneMonthAgoX = billsOneMonthAgo;

        if (billsOneMonthAgo > billsTwoMonthsAgoX) {
            billsOneMonthAgo = billsOneMonthAgo - billsTwoMonthsAgoX;
            balanceOneMonthsAgo = billsNotPaid;

            if (totalPayments >= billsOneMonthAgo) {
                billsNotPaid = 0d;
                balanceOneMonthsAgo = 0d;
                totalPayments = totalPayments - billsOneMonthAgo;
            } else if (totalPayments < billsOneMonthAgo && totalPayments > 0) {
                billsNotPaid = billsOneMonthAgo - totalPayments;
                balanceOneMonthsAgo = billsOneMonthAgo - totalPayments;
                totalPayments = 0d;
            } else {
                //Nothing paid
                billsNotPaid = billsOneMonthAgo;
                balanceOneMonthsAgo = billsOneMonthAgo;
                totalPayments = 0d;
            }
        }


        //Today
        Double billsToday = getBillsByDate(accountId, today) + balanceBroughtForward;
        if (billsToday > billsOneMonthAgoX) {
            billsToday = billsToday - billsOneMonthAgoX;
            balanceToday = billsNotPaid;

            if (totalPayments >= billsToday) {
                billsNotPaid = 0d;
                balanceToday = 0d;
                totalPayments = totalPayments - billsToday;
            } else if (totalPayments < billsToday && totalPayments > 0) {
                billsNotPaid = billsToday - totalPayments;
                balanceToday = billsToday - totalPayments;
                totalPayments = 0d;
            } else {
                //Nothing paid
                billsNotPaid = billsToday;
                balanceToday = billsToday;
                totalPayments = 0d;
            }
        }

        AgeingData ageingRecord = new AgeingData();
        Double balance = 0d;
        if (totalPayments > 0) {
            balance = (-1 * totalPayments);
        } else {
            balance = balanceToday + balanceOneMonthsAgo + balanceTwoMonthsAgo + balanceThreeMonthsAgo + balanceFourMonthsAgo + balanceSixMonthsAgo;
        }

        //log.info("Ageing report for " + accNo);
        ageingRecord.setName(consumerName);
        ageingRecord.setAccNo(accNo);
        ageingRecord.setBalanceToday(balanceToday);
        ageingRecord.setBalanceOneMonthsAgo(balanceOneMonthsAgo);
        ageingRecord.setBalanceTwoMonthsAgo(balanceTwoMonthsAgo);
        ageingRecord.setBalanceThreeMonthsAgo(balanceThreeMonthsAgo);
        ageingRecord.setBalanceFourMonthsAgo(balanceFourMonthsAgo);
        ageingRecord.setBalanceSixMonthsAgo(balanceSixMonthsAgo);
        ageingRecord.setAccountId(accountId);
        //ageingRecord.setUserId(userId);
        ageingRecord.setAccNo(accNo);
        ageingRecord.setCutOff(accountStatus);
        ageingRecord.setBalance(balance);//To
        ageingRecord.setReportHeader(reportHeader);
        ageingRecord.setZone(zone);
        ageingDataRepository.save(ageingRecord);
    }

    @Transactional
    public Double getAccountBalance(Long accountId) {
        //this.updateBalance(accountId);

        // update balances
        Account account = accountRepository.findOne(accountId);

        return account.getOutstandingBalance();


//        if (!account.isMetered()) {
//            account.setMeter(null);
//        }
//        Double balance = 0d;
//
//        // add balance b/f
//        balance += account.getBalanceBroughtForward();
//
//        Double waterSaleTotal = balance;
//        Double meterRentTotal = 0d;
//        Double penaltiesTotal = 0d;
//
//        List<Bill> bills = account.getBills();
//        if (bills != null) if (!bills.isEmpty()) {
//            {
//                for (Bill bill : bills) {
//                    balance += bill.getAmount();
//                    balance += bill.getMeterRent();
//
//                    //granular balances
//                    waterSaleTotal += bill.getAmount();
//                    meterRentTotal += bill.getMeterRent();
//
//                    if (bill.getBillItems() != null) {
//                        // get bill items
//                        List<BillItem> billItems = bill.getBillItems();
//                        if (!billItems.isEmpty()) {
//                            for (BillItem billItem : billItems) {
//                                balance += billItem.getAmount();
//                                penaltiesTotal += billItem.getAmount();
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        Double totalPayments = 0d;
//        // get payments
//        List<Payment> payments = account.getPayments();
//        if (payments != null) {
//            if (!payments.isEmpty()) {
//                for (Payment p : payments) {
//                    balance = (balance - p.getAmount());
//                    totalPayments += p.getAmount();
//                }
//            }
//        }

    }

    @Transactional
    public RestResponse create(RestRequestObject<Account> requestObject, Long id) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_create");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Account account = requestObject.getObject();
                Account acc = accountRepository.findByaccNo(account.getAccNo());

                Consumer consumer = consumerRepository.findOne(id);

                if (acc != null) {
                    responseObject.setMessage("Account already exists");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else if (consumer == null) {
                    responseObject.setMessage("Invalid consumer");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                } else {
                    // create resource
                    acc = new Account();
                    acc.setAccNo(account.getAccNo());
                    acc.setActive(Boolean.FALSE);
                    acc.setOnStatus(OnStatus.PENDING);
                    acc.setAverageConsumption(account.getAverageConsumption());
                    acc.setBalanceBroughtForward(account.getBalanceBroughtForward());
                    if (StringUtils.isNotEmpty(account.getPhoneNumber())) {
                        acc.setPhoneNumber(account.getPhoneNumber());
                    }
                    if (StringUtils.isNotEmpty(account.getNotes())) {
                        acc.setNotes(account.getNotes());
                    }
                    Account created = accountRepository.save(acc);

                    created.setLocation(account.getLocation());
                    created.setZone(account.getZone());
                    created.setTariff(account.getTariff());
                    created.setConsumer(consumer);
                    created.setAccountCategory(account.getAccountCategory());
                    //created.setScheme(account.getScheme());
                    accountRepository.save(created);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(created.getAccountId()));
                    auditRecord.setParentObject("Account");
                    auditRecord.setCurrentData(created.toString());
                    auditRecord.setNotes("CREATED ACCOUNT");
                    auditService.log(AuditOperation.CREATED, auditRecord);
                    //End - audit trail

                    // package response
                    responseObject.setMessage("Account created successfully. ");
                    responseObject.setPayload(created);
                    response = new RestResponse(responseObject, HttpStatus.CREATED);
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @Transactional
    public RestResponse update(RestRequestObject<Account> requestObject, Long id) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Account account = requestObject.getObject();
                Account acc = accountRepository.findOne(id);

                if (acc == null) {
                    responseObject.setMessage("Account not found");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                } else if (acc.getOnStatus() == OnStatus.TURNED_OFF) {
                    responseObject.setMessage("Sorry, we could not complete your request, the account has been turned off.");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                } else {
                    // setup resource
                    // TODO;
                    acc.setLocation(account.getLocation());
                    acc.setZone(account.getZone());
                    acc.setTariff(account.getTariff());
                    acc.setAccNo(account.getAccNo());
                    acc.setAverageConsumption(account.getAverageConsumption());
                    //acc.setBalanceBroughtForward(account.getBalanceBroughtForward());
                    acc.setAccountCategory(account.getAccountCategory());
                    acc.setBillingFrequency(account.getBillingFrequency());

                    if (StringUtils.isNotEmpty(account.getPhoneNumber())) {
                        acc.setPhoneNumber(account.getPhoneNumber());
                    }
                    if (StringUtils.isNotEmpty(account.getNotes())) {
                        acc.setNotes(account.getNotes());
                    }

                    // save
                    acc = accountRepository.save(acc);
                    responseObject.setMessage("Account  updated successfully");
                    responseObject.setPayload(acc);
                    response = new RestResponse(responseObject, HttpStatus.OK);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(acc.getAccountId()));
                    auditRecord.setParentObject("Account");
                    auditRecord.setCurrentData(acc.toString());
                    auditRecord.setPreviousData(account.toString());
                    auditRecord.setNotes("UPDATED ACCOUNT");
                    auditService.log(AuditOperation.UPDATED, auditRecord);
                    //End - audit trail
                }
            }
        } catch (org.hibernate.exception.ConstraintViolationException ex) {
            responseObject.setMessage("Duplicate account no");
            responseObject.setPayload("");
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            responseObject.setPayload("");
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }


    @Transactional
    public RestResponse updateStatus(RestRequestObject<AccountStatusHistory> requestObject, Long accountId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                Account account = accountRepository.findOne(accountId);

                if (account == null) {
                    responseObject.setMessage("Account not found");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                } else {
                    AccountStatusHistory accountStatusHistory = requestObject.getObject();
                    if (accountStatusHistory.getNotes().isEmpty()) {
                        responseObject.setMessage("Notes missing");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }

                    accountStatusHistory.setAccount(account);

                    if (account.isActive()) {
                        response = authManager.grant(requestObject.getToken(), "account_deactivate");
                        if (response.getStatusCode() != HttpStatus.OK) {
                            return response;
                        }
                        accountStatusHistory.setStatusType("DEACTIVATED");
                        account.setActive(false);
                    } else {
                        response = authManager.grant(requestObject.getToken(), "account_activate");
                        if (response.getStatusCode() != HttpStatus.OK) {
                            return response;
                        }
                        accountStatusHistory.setStatusType("ACTIVATED");
                        account.setActive(true);
                    }

                    // save
                    accountRepository.save(account);

                    //save history
                    accountStatusHistoryRepository.save(accountStatusHistory);

                    //send back payload
                    responseObject.setMessage("Account  updated successfully");
                    responseObject.setPayload(account);
                    response = new RestResponse(responseObject, HttpStatus.OK);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(account.getAccountId()));
                    auditRecord.setParentObject("Account");
                    auditRecord.setCurrentData(account.toString());
                    auditRecord.setNotes("UPDATED ACCOUNT");
                    auditService.log(AuditOperation.UPDATED, auditRecord);
                    //End - audit trail
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            responseObject.setPayload("");
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @Transactional
    public RestResponse turnOnOffAccount(RestRequestObject<AccountStatusHistory> requestObject, Long accountId) {
        try {
            String emailAddress = "";
            String actionName = "TURNED ON";
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_update");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                Account account = accountRepository.findOne(accountId);

                emailAddress = authManager.getEmailFromToken(requestObject.getToken());

                if (account == null) {
                    responseObject.setMessage("Account not found");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                } else {
                    if (account.getOnStatus() == OnStatus.TURNED_OFF) {
                        responseObject.setMessage("Sorry we can not complete your request. Account already turned off.");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }

                    AccountStatusHistory accountStatusHistory = requestObject.getObject();
                    if (accountStatusHistory.getNotes().isEmpty()) {
                        responseObject.setMessage("Notes missing");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                        return response;
                    }
                    accountStatusHistory.setAccount(account);

                    if (account.getOnStatus() == OnStatus.PENDING) {
                        response = authManager.grant(requestObject.getToken(), "account_turnOn");
                        if (response.getStatusCode() != HttpStatus.OK) {
                            return response;
                        }

                        accountStatusHistory.setStatusType("TURNED_ON");
                        account.setActive(Boolean.TRUE);
                        account.setOnStatus(OnStatus.TURNED_ON);
                        account.setTurnedOnBy(emailAddress);
                        account.setDateTurnedOn(new DateTime());
                        actionName = "TURNED ON";
                    } else if (account.getOnStatus() == OnStatus.TURNED_ON) {
                        response = authManager.grant(requestObject.getToken(), "account_turnOff");
                        if (response.getStatusCode() != HttpStatus.OK) {
                            return response;
                        }
                        accountStatusHistory.setStatusType("TURNED_OFF");
                        account.setActive(Boolean.FALSE);
                        account.setOnStatus(OnStatus.TURNED_OFF);
                        account.setTurnedOffBy(emailAddress);
                        account.setDateTurnedOff(new DateTime());
                        account.setNotes(accountStatusHistory.getNotes());
                        actionName = "TURNED OFF";
                    }

                    // save

                    account = accountRepository.save(account);

                    accountStatusHistory.setAccount(account);

                    //save history
                    accountStatusHistoryRepository.save(accountStatusHistory);

                    //send back payload
                    responseObject.setMessage("Account  updated successfully");
                    responseObject.setPayload(account);
                    response = new RestResponse(responseObject, HttpStatus.OK);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(account.getAccountId()));
                    auditRecord.setParentObject("Account");
                    auditRecord.setCurrentData(account.toString());
                    auditRecord.setNotes("UPDATED ACCOUNT - " + actionName);
                    auditService.log(AuditOperation.UPDATED, auditRecord);
                    //End - audit trail
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            responseObject.setPayload("");
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @Transactional
    public RestResponse transfer(RestRequestObject<Account> requestObject, Long id) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_transfer");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Account account = requestObject.getObject();

                Account acc = accountRepository.findOne(account.getAccountId());
                Consumer consumer = consumerRepository.findOne(id);
                if (acc == null) {
                    responseObject.setMessage("Invalid account");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                } else if (consumer == null) {
                    responseObject.setMessage("Invalid consumer");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                } else {
                    // set new consumer
                    acc.setConsumer(consumer);

                    // save
                    accountRepository.save(acc);
                    responseObject.setMessage("Account  updated successfully");
                    responseObject.setPayload(acc);
                    response = new RestResponse(responseObject, HttpStatus.OK);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(acc.getAccountId()));
                    auditRecord.setParentObject("Account");
                    auditRecord.setCurrentData(String.valueOf(acc.getConsumer().getConsumerId()));
                    auditRecord.setPreviousData(String.valueOf(consumer.getConsumerId()));
                    auditRecord.setNotes("TRANSFERRED ACCOUNT");
                    auditService.log(AuditOperation.UPDATED, auditRecord);
                    //End - audit trail
                }
            }
        } catch (org.hibernate.exception.ConstraintViolationException ex) {
            responseObject.setMessage("Duplicate account no");
            responseObject.setPayload("");
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            responseObject.setPayload("");
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    public RestResponse getAllByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "accounts_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                RestPageRequest p = requestObject.getObject();

                BooleanBuilder builder = new BooleanBuilder();
                if (StringUtils.isNotEmpty(p.getFilter())) {
                    builder.or(QAccount.account.accNo.containsIgnoreCase(p.getFilter()));
                }

                BooleanBuilder nameBuilder = new BooleanBuilder();
                nameBuilder.or(QAccount.account.consumer.firstName.containsIgnoreCase(p.getFilter()));
                nameBuilder.or(QAccount.account.consumer.lastName.containsIgnoreCase(p.getFilter()));
                nameBuilder.or(QAccount.account.consumer.middleName.containsIgnoreCase(p.getFilter()));
                nameBuilder.or(QAccount.account.consumer.phoneNumber.containsIgnoreCase(p.getFilter()));
                nameBuilder.or(QAccount.account.consumer.identityNo.containsIgnoreCase(p.getFilter()));
                builder.or(nameBuilder);

                Page<Account> page = accountRepository.findAll(builder, new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));

                if (page.hasContent()) {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(page);
                    response = new RestResponse(responseObject, HttpStatus.OK);
                } else {
                    responseObject.setMessage("Your search did not match any records");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getAllByConsumer(RestRequestObject<RestPageRequest> requestObject, Long consumerId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                Consumer consumer = consumerRepository.findOne(consumerId);
                if (consumer == null) {
                    responseObject.setMessage("Invalid consumer info");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else {
                    List<Account> accounts = consumer.getAccounts();
                    if (accounts.size() > 0) {
                        responseObject.setMessage("Fetched data successfully");
                        responseObject.setPayload(accounts);
                        response = new RestResponse(responseObject, HttpStatus.OK);
                    } else {
                        responseObject.setMessage("Your search did not match any records");
                        responseObject.setPayload("");
                        response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                    }
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return response;
    }

    public RestResponse getSchemeList(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                List<Scheme> schemeList = schemeRepository.findAll();
                if (schemeList.isEmpty()) {
                    responseObject.setMessage("Your search did not match any records");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(schemeList);
                    response = new RestResponse(responseObject, HttpStatus.OK);
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return response;
    }

    @Transactional
    public void setUpdateBalance(Long accountId) {
        Account account = accountRepository.findOne(accountId);
        account.setUpdateBalance(true);
        accountRepository.save(account);
    }

    public RestResponse getById(RestRequestObject<RestPageRequest> requestObject, Long id) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                updateBalance(id);
                Account account = accountRepository.findOne(id);
                if (account == null) {
                    responseObject.setMessage("Invalid account number");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                } else {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(account);
                    response = new RestResponse(responseObject, HttpStatus.OK);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(account.getAccountId()));
                    auditRecord.setParentObject("Account");
                    auditRecord.setNotes("ACCOUNT PROFILE VIEW");
                    auditService.log(AuditOperation.VIEWED, auditRecord);
                    //End - audit trail
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getOne(RestRequestObject<Account> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "account_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }
                Account acc = requestObject.getObject();
                log.info("Find account by account no:" + acc.getAccNo());
                Account account = accountRepository.findByaccNo(acc.getAccNo());


                if (account == null) {
                    responseObject.setMessage("Invalid account number");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);

                } else {
                    this.updateBalance(account.getAccountId());
                    account = accountRepository.findOne(account.getAccountId());
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(account);
                    response = new RestResponse(responseObject, HttpStatus.OK);

                    //Start - audit trail
                    AuditRecord auditRecord = new AuditRecord();
                    auditRecord.setParentID(String.valueOf(account.getAccountId()));
                    auditRecord.setParentObject("Account");
                    auditRecord.setNotes("ACCOUNT SEARCH VIEW");
                    auditService.log(AuditOperation.VIEWED, auditRecord);
                    //End - audit trail
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public Double getAccountBalanceByDate(Long accountId, DateTime toDate) {
        Double balance = 0d;
        // update balances
        balance += billService.getAccountBillsByDateWithBalanceBF(accountId, toDate);
        balance -= paymentService.getTotalByAccountByDate(accountId, toDate.hourOfDay().withMaximumValue());
        return balance;
    }

    public Double getAccountBalanceByTransDate(Long accountId, DateTime toDate) {
        Double balance = 0d;
        // update balances
        Double bills = billService.getAccountBillsByTransDateWithBalanceBF(accountId, toDate.hourOfDay().withMaximumValue());
        Double payments = paymentService.getTotalByAccountByDate(accountId, toDate.hourOfDay().withMaximumValue());
        balance = bills - payments;
        return balance;
    }

    public RestResponse getAccountsReceivables(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_account_receivable");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                log.info("Getting account balances params");
                ReportsParam request = requestObject.getObject();
                Map<String, String> params = new HashMap<>();

                if (request.getFields() != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonString = mapper.writeValueAsString(request.getFields());
                    log.info("ParamsJSON:" + jsonString);
                    params = mapper.readValue(jsonString, Map.class);

                }

                log.info("Generating account balances report");
                List<BigInteger> accountIds = accountRepository.findAllAccountIds();

                Double totalAmount = 0.0;
                if (!accountIds.isEmpty()) {
                    log.info(accountIds.size() + " accounts found.");
                    List<BalancesReport> balances = new ArrayList<>();
                    Integer count = 1;
                    for (BigInteger accountId : accountIds) {
                        Account acc = accountRepository.findOne(accountId.longValue());
                        BalancesReport br = new BalancesReport();
                        br.setAccName(acc.getAccName());
                        br.setAccNo(acc.getAccNo());
                        br.setZone(acc.getZone().getName());

                        br.setBalance(acc.getOutstandingBalance());

                        br.setActive(acc.isActive());

                        //log.info("getting balance for " + count);
                        count++;
                        Boolean include = true;

                        if (params != null) {
                            if (!params.isEmpty()) {
                                //transactionDate=null, zoneId=7, accountStatus=Active, creditBalances=Exclude
                                //get balance based on transaction date

                                //zone id
                                if (params.containsKey("zoneId")) {
                                    Object zoneId = params.get("zoneId");
                                    if (acc.getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                        include = false;
                                    }
                                }
                                //account status
                                if (params.containsKey("accountStatus")) {
                                    String status = params.get("accountStatus");
                                    if (status.compareToIgnoreCase("inactive") == 0) {
                                        if (acc.isActive()) {
                                            include = false;
                                        }
                                    } else if (status.compareToIgnoreCase("active") == 0) {
                                        if (!acc.isActive()) {
                                            include = false;
                                        }
                                    }
                                }
                            }
                        }

                        if (include) {
                            //get balance based on date
                            if (params.containsKey("transactionDate")) {
                                Calendar calendar = Calendar.getInstance();
                                Object unixTime = params.get("transactionDate");
                                if (unixTime.toString().compareToIgnoreCase("null") != 0) {
                                    calendar.setTimeInMillis(Long.valueOf(unixTime.toString()) * 1000);
                                }

                                //update balance on local object
                                //br.setBalance(this.getAccountBalanceByDate(acc, calendar));
                            }

                            if (br.getBalance() > 0) {
                                totalAmount += br.getBalance();
                                balances.add(br);
                            }
                        }
                    }
                    log.info("Packaged report data...");


                    ReportObject report = new ReportObject();
                    report.setAmount(totalAmount);
                    report.setDate(Calendar.getInstance());

                    report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                    report.setTitle(this.optionService.getOption("REPORT:ACCOUNTS_RECEIVABLE").getValue());
                    report.setContent(balances);
                    log.info("Sending Payload send to client...");
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(report);
                    response = new RestResponse(responseObject, HttpStatus.OK);
                } else {
                    responseObject.setMessage("Your search did not match any records");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getCreditBalances(RestRequestObject<ReportsParam> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_credit_balances");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                log.info("Getting credit balances params");
                ReportsParam request = requestObject.getObject();
                Map<String, String> params = new HashMap<>();

                if (request.getFields() != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonString = mapper.writeValueAsString(request.getFields());
                    log.info("ParamsJSON:" + jsonString);
                    params = mapper.readValue(jsonString, Map.class);

                }

                log.info("Generating credit balances report");

                List<BigInteger> accountList = accountRepository.findAllAccountIds();

                Double totalAmount = 0.0;
                if (!accountList.isEmpty()) {
                    log.info(accountList.size() + " accounts found.");
                    List<BalancesReport> balances = new ArrayList<>();

                    for (BigInteger accountId : accountList) {
                        Account acc = accountRepository.findOne(accountId.longValue());
                        BalancesReport br = new BalancesReport();
                        br.setAccName(acc.getAccName());
                        br.setAccNo(acc.getAccNo());
                        br.setZone(acc.getZone().getName());
                        br.setBalance(acc.getOutstandingBalance());
                        br.setActive(acc.isActive());

                        Boolean include = true;
                        if (params != null) {
                            if (!params.isEmpty()) {
                                //zone id
                                if (params.containsKey("zoneId")) {
                                    Object zoneId = params.get("zoneId");
                                    if (acc.getZone().getZoneId() != Long.valueOf(zoneId.toString())) {
                                        include = false;
                                    }
                                }
                                //account status
                                if (params.containsKey("accountStatus")) {
                                    String status = params.get("accountStatus");
                                    if (status.compareToIgnoreCase("inactive") == 0) {
                                        if (acc.isActive()) {
                                            include = false;
                                        }
                                    } else if (status.compareToIgnoreCase("active") == 0) {
                                        if (!acc.isActive()) {
                                            include = false;
                                        }
                                    }
                                }
                            }
                        }

                        if (include) {
                            //get balance based on date
                            if (params.containsKey("transactionDate")) {
                                Calendar calendar = Calendar.getInstance();
                                Object unixTime = params.get("transactionDate");
                                if (unixTime.toString().compareToIgnoreCase("null") != 0) {
                                    calendar.setTimeInMillis(1000 * Long.valueOf(unixTime.toString()));
                                }

                                //update balance on local object
                                //br.setBalance(this.getAccountBalanceByDate(acc, calendar));
                            }

                            if (br.getBalance() < 0) {
                                totalAmount += (br.getBalance());
                                balances.add(br);
                            }
                        }
                    }
                    log.info("Packaged report data...");


                    ReportObject report = new ReportObject();
                    report.setAmount(totalAmount);
                    report.setDate(Calendar.getInstance());


                    report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                    report.setTitle(this.optionService.getOption("REPORT:CREDIT_BALANCES").getValue());
                    report.setContent(balances);
                    log.info("Sending Payload send to client...");
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(report);
                    response = new RestResponse(responseObject, HttpStatus.OK);
                } else {
                    responseObject.setMessage("Your search did not match any records");
                    response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getFieldCardReport(RestRequestObject<AccountsReportRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_field_report");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                AccountsReportRequest accountsReportRequest = requestObject.getObject();
                BooleanBuilder builder = new BooleanBuilder();
                if (accountsReportRequest.getOnStatus() != null) {
                    builder.and(QAccount.account.onStatus.eq(accountsReportRequest.getOnStatus()));
                }

                if (accountsReportRequest.getIsCutOff() != null) {
                    Boolean isActive = Boolean.TRUE;
                    if (accountsReportRequest.getIsCutOff()) {
                        isActive = Boolean.FALSE;
                    }
                    builder.and(QAccount.account.active.eq(isActive));
                }

                if (accountsReportRequest.getZoneId() != null) {
                    builder.and(QAccount.account.zone.zoneId.eq(accountsReportRequest.getZoneId()));
                } else if (accountsReportRequest.getSchemeId() != null) {
                    builder.and(QAccount.account.zone.scheme.schemeId.eq(accountsReportRequest.getSchemeId()));
                }

                Iterable<Account> accountList = accountRepository.findAll(builder);
                List<AccountRecord> accountRecords = new ArrayList<>();

                for (Account acc : accountList) {
                    AccountRecord ar = new AccountRecord();
                    ar.setAccName(acc.getAccName());
                    ar.setAccNo(acc.getAccNo());
                    ar.setZone(acc.getZone().getName());
                    ar.setLocation(acc.getLocation().getName());
                    ar.setActive(acc.isActive());

                    if (acc.isMetered()) {
                        ar.setMeterNo(acc.getMeter().getMeterNo());
                        ar.setMeterOwner(acc.getMeter().getMeterOwner().getName());
                    }
                    accountRecords.add(ar);
                }
                log.info("Packaged report data...");
                ReportObject report = new ReportObject();
                report.setDate(Calendar.getInstance());

                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                report.setTitle(this.optionService.getOption("REPORT:FIELD_CARD").getValue());
                report.setContent(accountRecords);
                log.info("Sending Payload send to client...");
                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(report);
                response = new RestResponse(responseObject, HttpStatus.OK);

            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getAccountsReport(RestRequestObject<AccountsReportRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_accounts");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                AccountsReportRequest accountsReportRequest = requestObject.getObject();
                BooleanBuilder builder = new BooleanBuilder();
                if (accountsReportRequest.getOnStatus() != null) {
                    builder.and(QAccount.account.onStatus.eq(accountsReportRequest.getOnStatus()));
                }

                if (accountsReportRequest.getIsCutOff() != null) {
                    Boolean isActive = Boolean.TRUE;
                    if (accountsReportRequest.getIsCutOff()) {
                        isActive = Boolean.FALSE;
                    }
                    builder.and(QAccount.account.active.eq(isActive));
                }

                if (accountsReportRequest.getZoneId() != null) {
                    builder.and(QAccount.account.zone.zoneId.eq(accountsReportRequest.getZoneId()));
                } else if (accountsReportRequest.getSchemeId() != null) {
                    builder.and(QAccount.account.zone.scheme.schemeId.eq(accountsReportRequest.getSchemeId()));
                }
                JPAQuery query = new JPAQuery(entityManager);
                List<AccountRecord> accountRecords = new ArrayList<>();
                List<Tuple> accountTupleList = query.from(QAccount.account).where(builder).list(QAccount.account.location.name, QAccount.account.accountId, QAccount.account.accNo, QAccount.account.consumer.phoneNumber, QAccount.account.createdOn, QAccount.account.active, QAccount.account.zone.name, QAccount.account.consumer.firstName, QAccount.account.consumer.middleName, QAccount.account.consumer.lastName);
                if (!accountTupleList.isEmpty()) {
                    for (Tuple accountTuple : accountTupleList) {
                        try {
                            Long accountId = accountTuple.get(QAccount.account.accountId);
                            AccountRecord ar = new AccountRecord();
                            String consumerName = accountTuple.get(QAccount.account.consumer.firstName) + " " + accountTuple.get(QAccount.account.consumer.middleName) + " " + accountTuple.get(QAccount.account.consumer.lastName);
                            consumerName = consumerName.replace("null", "").toUpperCase();
                            ar.setAccName(consumerName);
                            ar.setAccNo(accountTuple.get(QAccount.account.accNo));
                            if (accountTuple.get(QAccount.account.consumer.phoneNumber) != null) {
                                ar.setPhoneNo(accountTuple.get(QAccount.account.consumer.phoneNumber).replace("null", ""));
                            }
                            ar.setZone(accountTuple.get(QAccount.account.zone.name));
                            ar.setActive(accountTuple.get(QAccount.account.active));
                            ar.setCreatedOn(accountTuple.get(QAccount.account.createdOn));

                            query = new JPAQuery(entityManager);
                            BooleanBuilder meterBuilder = new BooleanBuilder();
                            meterBuilder.and(QAccount.account.accountId.eq(accountId));
                            List<Tuple> meterTupleList = query.from(QAccount.account).where(meterBuilder).list(QAccount.account.meter.meterNo, QAccount.account.meter.meterSize.size, QAccount.account.meter.meterOwner.name);
                            if (meterTupleList != null) {
                                if (!meterTupleList.isEmpty()) {
                                    Tuple meterTuple = meterTupleList.get(0);
                                    ar.setMeterOwner(meterTuple.get(QAccount.account.meter.meterOwner.name));
                                    ar.setMeterNo(meterTuple.get(QAccount.account.meter.meterNo));
                                    ar.setMeterSize(meterTuple.get(QAccount.account.meter.meterSize.size));
                                }
                            }

                            query = new JPAQuery(entityManager);
                            BooleanBuilder categoryBuilder = new BooleanBuilder();
                            categoryBuilder.and(QAccount.account.accountId.eq(accountId));
                            String categoryName = query.from(QAccount.account).where(categoryBuilder).singleResult(QAccount.account.accountCategory.name);
                            if (StringUtils.isNotEmpty(categoryName)) {
                                ar.setCategory(categoryName);
                            }
                            ar.setLocation(accountTuple.get(QAccount.account.location.name));
                            accountRecords.add(ar);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }


                log.info("Packaged report data...");
                ReportObject report = new ReportObject();
                report.setDate(Calendar.getInstance());
                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                report.setTitle(this.optionService.getOption("REPORT:ACCOUNTS").getValue());
                report.setContent(accountRecords);
                log.info("Sending Payload send to client...");
                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(report);
                response = new RestResponse(responseObject, HttpStatus.OK);

                //responseObject.setMessage("Your search did not match any records");
                //response = new RestResponse(responseObject, HttpStatus.NOT_FOUND);

            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }
}
