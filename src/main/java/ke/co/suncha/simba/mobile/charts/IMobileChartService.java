package ke.co.suncha.simba.mobile.charts;

import ke.co.suncha.simba.mobile.charts.card.CardStatData;
import ke.co.suncha.simba.mobile.charts.line.LineGraphData;
import ke.co.suncha.simba.mobile.charts.pie.PieChartData;

import java.util.List;

/**
 * Created by maitha.manyala on 9/7/17.
 */
public interface IMobileChartService {
    CardStatData getReceiptsToday(List<Long> zoneList, int sequence);

    CardStatData getReceiptsYesterday(List<Long> zoneList, int sequence);

    CardStatData getSMSBalance(int sequence);

    CardStatData getReceiptsThisMonth(List<Long> zoneList, int sequence);

    PieChartData getMetersRead(List<Long> zoneList, int sequence);

    PieChartData getAccountsBilled(List<Long> zoneList, int sequence);

    CardStatData getTotalBalances(List<Long> zoneList, int sequence);

    CardStatData getTotalCreditBalances(List<Long> zoneList, int sequence);

    CardStatData getTotalAccounts(List<Long> zoneList, int sequence);

    PieChartData getAccounts(List<Long> zoneList, int sequence);

    PieChartData getReceiptsAndBilledThisMonth(List<Long> zoneList, int sequence);

    PieChartData getReceiptsAndBilledThisMonthCalc(List<Long> zoneList, int sequence);

    LineGraphData getReceiptsBillingOverPeriod(List<Long> zoneList, int sequence);
}
