package ke.co.suncha.simba.mobile.charts;

import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.service.UserService;
import ke.co.suncha.simba.aqua.scheme.zone.Zone;
import ke.co.suncha.simba.aqua.scheme.zone.meterReader.MeterReaderService;
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

    @Autowired
    UserService userService;

    @Autowired
    MeterReaderService meterReaderService;

    public RequestResponse<List<MobileChart>> getCharts(MobileUser mobileUser) {
        RequestResponse<List<MobileChart>> response = new RequestResponse<>();
        if (!mobileUserAuthService.canLogin(mobileUser)) {
            response.setError(Boolean.TRUE);
            response.setMessage("Access denied to this resource");
            return response;
        } else {
            User user = userService.getByEmailAddress(mobileUser.getEmail());
            List<Long> zoneIds = new ArrayList<>();
            if (!mobileUserAuthService.isMobileOrganizationLevel(user.getUserId())) {
                List<Zone> zoneList = meterReaderService.getZonesWithMeterReader(user.getUserId());
                for (Zone zone : zoneList) {
                    zoneIds.add(zone.getZoneId());
                }
            }


            List<MobileChart> mobileChartList = new ArrayList<>();
            mobileChartList.add(mobileChartService.getReceiptsToday(zoneIds, 1));
            mobileChartList.add(mobileChartService.getReceiptsYesterday(zoneIds, 2));
            mobileChartList.add(mobileChartService.getReceiptsAndBilledThisMonthCalc(zoneIds, 3));
            //mobileChartList.add(mobileChartService.getMetersRead(zoneIds,4));
            mobileChartList.add(mobileChartService.getAccountsBilled(zoneIds,5));
            mobileChartList.add(mobileChartService.getAccounts(zoneIds,6));
            mobileChartList.add(mobileChartService.getReceiptsThisMonth(zoneIds, 7));
            mobileChartList.add(mobileChartService.getReceiptsBillingOverPeriod(zoneIds, 8));
            mobileChartList.add(mobileChartService.getTotalAccounts(zoneIds,9));
            mobileChartList.add(mobileChartService.getTotalBalances(zoneIds,10));
            mobileChartList.add(mobileChartService.getTotalCreditBalances(zoneIds,11));

            Collections.sort(mobileChartList);

            response.setError(Boolean.FALSE);
            response.setMessage("Data fetched successfully");
            response.setObject(mobileChartList);
        }
        return response;
    }

}
