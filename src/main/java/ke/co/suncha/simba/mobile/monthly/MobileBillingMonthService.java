package ke.co.suncha.simba.mobile.monthly;

import ke.co.suncha.simba.aqua.models.BillingMonth;
import ke.co.suncha.simba.aqua.services.BillingMonthService;
import ke.co.suncha.simba.mobile.MobileUser;
import ke.co.suncha.simba.mobile.request.RequestResponse;
import ke.co.suncha.simba.mobile.user.MobileUserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by maitha.manyala on 8/30/17.
 */
@Service
public class MobileBillingMonthService {
    @Autowired
    MobileUserAuthService mobileUserAuthService;

    @Autowired
    BillingMonthService billingMonthService;

    public RequestResponse<MobileBillingMonth> getCurrent(MobileUser mobileUser) {
        RequestResponse<MobileBillingMonth> response = new RequestResponse<>();
        if (!mobileUserAuthService.canLogin(mobileUser)) {
            response.setError(Boolean.TRUE);
            response.setMessage("Access denied to this resource");
            return response;
        } else {

            BillingMonth billingMonth = billingMonthService.getActiveMonth();

            if (billingMonth != null) {
                MobileBillingMonth mBillingMonth = new MobileBillingMonth();
                mBillingMonth.setBillingMonth(billingMonth.getMonth().getMillis());
                mBillingMonth.setBillingMonthId(billingMonth.getBillingMonthId());
                mBillingMonth.setIsCurrent(billingMonth.getCurrent());

                Integer meterReading = 0;

                if (billingMonth.getMeterReading()) {
                    meterReading = 1;
                }

                mBillingMonth.setIsMeterReading(meterReading);

                response.setError(Boolean.FALSE);
                response.setMessage("Data fetched successfully");
                response.setObject(mBillingMonth);
            } else {
                response.setError(Boolean.TRUE);
                response.setMessage("No current billing month found");
            }
        }
        return response;
    }
}