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
            mobileChartList.add(mobileChartService.getReceiptsToday(1));
            mobileChartList.add(mobileChartService.getReceiptsYesterday(2));
            mobileChartList.add(mobileChartService.getReceiptsAndBilledThisMonthCalc(3));
            mobileChartList.add(mobileChartService.getMetersRead(4));
            mobileChartList.add(mobileChartService.getAccountsBilled(5));
            mobileChartList.add(mobileChartService.getAccounts(6));
            mobileChartList.add(mobileChartService.getReceiptsThisMonth(7));
            mobileChartList.add(mobileChartService.getReceiptsBillingOverPeriod(8));
            mobileChartList.add(mobileChartService.getTotalAccounts(9));
            mobileChartList.add(mobileChartService.getTotalBalances(10));
            mobileChartList.add(mobileChartService.getTotalCreditBalances(11));

            Collections.sort(mobileChartList);

            response.setError(Boolean.FALSE);
            response.setMessage("Data fetched successfully");
            response.setObject(mobileChartList);
        }
        return response;
    }

}
