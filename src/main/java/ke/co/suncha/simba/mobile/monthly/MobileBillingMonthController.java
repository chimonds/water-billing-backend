package ke.co.suncha.simba.mobile.monthly;

import ke.co.suncha.simba.mobile.MobileUser;
import ke.co.suncha.simba.mobile.request.RequestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by maitha.manyala on 8/30/17.
 */
@RestController
@RequestMapping(value = "/api/v1/mobile/billingMonths")
public class MobileBillingMonthController {
    @Autowired
    MobileBillingMonthService mobileBillingMonthService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public RequestResponse getCurrent(@RequestBody MobileUser mobileUser, HttpServletRequest request, HttpServletResponse response) {
        return mobileBillingMonthService.getCurrent(mobileUser);
    }
}