package ke.co.suncha.simba.mobile.charts;

import ke.co.suncha.simba.mobile.charts.card.CardStatData;
import ke.co.suncha.simba.mobile.charts.line.LineGraphData;
import ke.co.suncha.simba.mobile.charts.pie.PieChartData;

/**
 * Created by maitha.manyala on 9/7/17.
 */
public interface IMobileChartService {
    CardStatData getReceiptsToday(int sequence);

    CardStatData getReceiptsYesterday(int sequence);

    CardStatData getSMSBalance(int sequence);

    CardStatData getReceiptsThisMonth(int sequence);

    PieChartData getMetersRead(int sequence);

    PieChartData getAccountsBilled(int sequence);

    CardStatData getTotalBalances(int sequence);

    CardStatData getTotalCreditBalances(int sequence);

    CardStatData getTotalAccounts(int sequence);

    PieChartData getAccounts(int sequence);

    PieChartData getReceiptsAndBilledThisMonth(int sequence);

    PieChartData getReceiptsAndBilledThisMonthCalc(int sequence);

    LineGraphData getReceiptsBillingOverPeriod(int sequence);
}
