package ke.co.suncha.simba.mobile.charts;

import ke.co.suncha.simba.mobile.MobileUser;
import ke.co.suncha.simba.mobile.request.RequestResponse;
import ke.co.suncha.simba.mobile.user.MobileUserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by maitha.manyala on 9/7/17.
 */
@Service
public class MobileChartManager {
    @Autowired
    MobileChartService mobileChartService;

    @Autowired
    MobileUserAuthService mobileUserAuthService;

    public RequestResponse<List<MobileChart>> getCharts(MobileUser mobileUser) {
        RequestResponse<List<MobileChart>> response = new RequestResponse<>();
        if (!mobileUserAuthService.canLogin(mobileUser)) {
            response.setError(Boolean.TRUE);
            response.setMessage("Access denied to this resource");
            return response;
        } else {
            List<MobileChart> mobileChartList = new ArrayList<>();
            mobileChartList.add(mobileChartService.getReceiptsToday(10));
            mobileChartList.add(mobileChartService.getReceiptsYesterday(11));
            mobileChartList.add(mobileChartService.getReceiptsThisMonth(12));
            mobileChartList.add(mobileChartService.getReceiptsAndBilledThisMonthCalc(20));
            mobileChartList.add(mobileChartService.getMetersRead(21));
            mobileChartList.add(mobileChartService.getAccountsBilled(21));
            mobileChartList.add(mobileChartService.getAccounts(30));
            mobileChartList.add(mobileChartService.getReceiptsBillingOverPeriod(40));
            mobileChartList.add(mobileChartService.getTotalAccounts(50));
            mobileChartList.add(mobileChartService.getTotalBalances(60));
            mobileChartList.add(mobileChartService.getTotalCreditBalances(70));

            Collections.sort(mobileChartList);

            response.setError(Boolean.FALSE);
            response.setMessage("Data fetched successfully");
            response.setObject(mobileChartList);
        }
        return response;
    }

}
