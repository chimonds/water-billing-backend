package ke.co.suncha.simba.mobile.charts;

import ke.co.suncha.simba.aqua.account.AccountService;
import ke.co.suncha.simba.aqua.billing.BillingService;
import ke.co.suncha.simba.aqua.receipts.ReceiptService;
import ke.co.suncha.simba.aqua.services.BillingMonthService;
import ke.co.suncha.simba.mobile.MobileUtil;
import ke.co.suncha.simba.mobile.charts.card.CardStatData;
import ke.co.suncha.simba.mobile.charts.line.LineEntry;
import ke.co.suncha.simba.mobile.charts.line.LineGraphData;
import ke.co.suncha.simba.mobile.charts.line.LineGraphDataset;
import ke.co.suncha.simba.mobile.charts.pie.PEntry;
import ke.co.suncha.simba.mobile.charts.pie.PieChartData;
import ke.co.suncha.simba.mobile.upload.MeterReadingRecordService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maitha.manyala on 9/7/17.
 */
@Service
public class MobileChartService implements IMobileChartService {
    @Autowired
    AccountService accountService;

    @Autowired
    BillingService billingService;

    @Autowired
    ReceiptService receiptService;

    @Autowired
    BillingMonthService billingMonthService;

    @Autowired
    MeterReadingRecordService meterReadingRecordService;

    @Override
    public CardStatData getReceiptsToday(List<Long> zoneList, int sequence) {
        Double amount = receiptService.getReceiptsToday(zoneList);
        CardStatData data = new CardStatData();
        data.setLabel("Receipts Today so far");
        data.setValue(MobileUtil.getAmountPlain(amount) + " KES");
        data.setSequence(sequence);
        return data;
    }

    @Override
    public CardStatData getSMSBalance(int sequence) {
        return null;
    }

    @Override
    public CardStatData getTotalBalances(List<Long> zoneList, int sequence) {
        Double amount = accountService.getTotalBalances(zoneList);
        CardStatData data = new CardStatData();
        data.setLabel("Balances");
        data.setValue(MobileUtil.getAmountPlain(amount) + " KES");
        data.setSequence(sequence);
        return data;
    }

    @Override
    public CardStatData getTotalCreditBalances(List<Long> zoneList, int sequence) {
        Double amount = accountService.getTotalCreditBalances(zoneList);
        CardStatData data = new CardStatData();
        data.setLabel("Credit Balances");
        data.setValue(MobileUtil.getAmountPlain(amount) + " KES");
        data.setSequence(sequence);
        return data;
    }

    @Override
    public CardStatData getTotalAccounts(List<Long> zoneList, int sequence) {
        Long count = accountService.getTotalAccounts(zoneList);
        CardStatData data = new CardStatData();
        data.setLabel("Total Accounts");
        data.setValue(MobileUtil.getAmountPlain(Double.parseDouble(count.toString())));
        data.setSequence(sequence);
        return data;
    }

    @Override
    public PieChartData getAccounts(List<Long> zoneList, int sequence) {
        Long activeAccounts = accountService.getTotalActiveAccounts(zoneList);
        Long inActiveAccounts = accountService.getTotalInActiveAccounts(zoneList);
        Long totalAccount = activeAccounts + inActiveAccounts;
        PieChartData data = new PieChartData();
        data.setSequence(sequence);
        data.setName("Accounts");
        data.setCenterText(MobileUtil.getAmountPlain(totalAccount.doubleValue()) + " Accounts");

        List<PEntry> entries = new ArrayList<>();
        entries.add(new PEntry("Active", activeAccounts));
        entries.add(new PEntry("In Active", inActiveAccounts));
        data.setEntries(entries);

        List<String> colours = new ArrayList<>();
        colours.add("#00BF9A");
        colours.add("#FF8A65");

        data.setColours(colours);
        data.setValueTextColour("#FFFFFF");
        return data;
    }

    @Override
    public PieChartData getReceiptsAndBilledThisMonth(List<Long> zoneList, int sequence) {
        Long billingMonthId = billingMonthService.getActiveMonthId();
        if (billingMonthId == null) {
            billingMonthId = 0l;
        } else {
            billingMonthId = billingMonthId - 1;
        }

        Double receiptsThisMonth = receiptService.getReceiptsThisMonth(zoneList);
        Double billedLastMonth = billingService.getBilledInMonth(zoneList, billingMonthId);
        Double percentage = receiptsThisMonth / billedLastMonth * 100;

        if (percentage.isNaN()) {
            percentage = 0.0;
        }

        PieChartData data = new PieChartData();
        data.setSequence(sequence);
        data.setName("This Month");
        data.setCenterText(percentage.intValue() + "% Efficiency");

        List<PEntry> entries = new ArrayList<>();
        entries.add(new PEntry("Receipts", receiptsThisMonth.floatValue()));
        entries.add(new PEntry("Billed", billedLastMonth.floatValue()));
        data.setEntries(entries);

        List<String> colours = new ArrayList<>();
        colours.add("#00BF9A");
        colours.add("#FF8A65");

        data.setColours(colours);
        data.setValueTextColour("#FFFFFF");
        return data;
    }

    @Override
    public PieChartData getReceiptsAndBilledThisMonthCalc(List<Long> zoneList, int sequence) {
        Long billingMonthId = billingMonthService.getActiveMonthId();
        if (billingMonthId == null) {
            billingMonthId = 0l;
        } else {
            billingMonthId = billingMonthId - 1;
        }

        Double receiptsThisMonth = receiptService.getReceiptsThisMonthCalculated(zoneList);
        Double billedLastMonth = billingService.getBilledInMonth(zoneList, billingMonthId);
        Double percentage = receiptsThisMonth / billedLastMonth * 100;
        if (percentage.isNaN()) {
            percentage = 0.0;
        }

        PieChartData data = new PieChartData();
        data.setSequence(sequence);
        data.setName("This Month");
        data.setCenterText(percentage.intValue() + "% Actual Efficiency");

        List<PEntry> entries = new ArrayList<>();
        entries.add(new PEntry("Receipts", receiptsThisMonth.floatValue()));
        entries.add(new PEntry("Billed", billedLastMonth.floatValue()));
        data.setEntries(entries);

        List<String> colours = new ArrayList<>();
        colours.add("#00BF9A");
        colours.add("#FF8A65");

        data.setColours(colours);
        data.setValueTextColour("#FFFFFF");
        return data;
    }

    @Override
    public LineGraphData getReceiptsBillingOverPeriod(List<Long> zoneList, int sequence) {
        Integer period = 6;
        Long billingMonthId = billingMonthService.getActiveMonthId();
        LineGraphData data = new LineGraphData();
        DateTime startOfMonth = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay().hourOfDay().withMinimumValue();
        LineGraphDataset billsDataSet = new LineGraphDataset();
        billsDataSet.setName("Bills");
        billsDataSet.setColor("#FF8A65");
        billsDataSet.setCircleColor("#FF8A65");
        List<LineEntry> billsLineEntries = new ArrayList<>();
        for (int x = 1; x <= period; x++) {
            Double amount = billingService.getBilledInMonth(zoneList, billingMonthId);
            billingMonthId--;
            LineEntry entry = new LineEntry();
            entry.setX(startOfMonth.plusDays(10).getMillis());
            entry.setY(amount.floatValue());
            billsLineEntries.add(entry);
            startOfMonth = startOfMonth.minusMonths(1);
        }
        billsDataSet.setLineEntries(billsLineEntries);

        List<LineGraphDataset> lineGraphDatasetList = new ArrayList<>();
        lineGraphDatasetList.add(billsDataSet);
        data.setLineGraphDatasetList(lineGraphDatasetList);
        data.setSequence(sequence);
        return data;
    }

    @Override
    public CardStatData getReceiptsYesterday(List<Long> zoneList, int sequence) {
        CardStatData data = new CardStatData();
        data.setSequence(sequence);
        data.setValue(MobileUtil.getAmountPlain(receiptService.getReceiptsYesterday(zoneList)) + " KES");
        data.setLabel("Receipts Yesterday");
        return data;
    }

    @Override
    public CardStatData getReceiptsThisMonth(List<Long> zoneList, int sequence) {
        CardStatData data = new CardStatData();
        data.setSequence(sequence);
        data.setValue(MobileUtil.getAmountPlain(receiptService.getReceiptsThisMonth(zoneList)) + " KES");
        data.setLabel("Receipts This Month");
        return data;
    }

    @Override
    public PieChartData getMetersRead(List<Long> zoneList, int sequence) {
        Long billingMonthId = billingMonthService.getActiveMonthId();
        Long accountsWithMeters = meterReadingRecordService.accountsWithMeters();
        Long metersRead = meterReadingRecordService.readByBillingMonth(billingMonthId);

        Long percentage = (metersRead / accountsWithMeters * 100);
        PieChartData data = new PieChartData();
        data.setSequence(sequence);
        data.setName("Meter Reading");
        data.setCenterText(percentage.intValue() + "% Read");

        List<PEntry> entries = new ArrayList<>();
        entries.add(new PEntry("Meters", accountsWithMeters.floatValue()));
        entries.add(new PEntry("Read", metersRead.floatValue()));
        data.setEntries(entries);

        List<String> colours = new ArrayList<>();
        colours.add("#00BF9A");
        colours.add("#FF8A65");

        data.setColours(colours);
        data.setValueTextColour("#FFFFFF");
        return data;
    }

    @Override
    public PieChartData getAccountsBilled(List<Long> zoneList, int sequence) {
        Long billingMonthId = billingMonthService.getActiveMonthId();

        Long accountsBilled = billingService.getTotalAccountsBilled(zoneList, billingMonthId);
        Long activeAccounts = accountService.getTotalActiveAccounts(zoneList);
        Double percentage = Double.valueOf(accountsBilled) / Double.valueOf(activeAccounts) * 100;


        //Long percentage = (accountsBilled / activeAccounts * 100);
        PieChartData data = new PieChartData();
        data.setSequence(sequence);
        data.setName("Billing");
        data.setCenterText(MobileUtil.getAmountPlain(percentage) + "% Billed");

        List<PEntry> entries = new ArrayList<>();
        entries.add(new PEntry("Accounts", activeAccounts.floatValue()));
        entries.add(new PEntry("Billed", accountsBilled.floatValue()));
        data.setEntries(entries);

        List<String> colours = new ArrayList<>();
        colours.add("#00BF9A");
        colours.add("#FF8A65");

        data.setColours(colours);
        data.setValueTextColour("#FFFFFF");
        return data;
    }
}