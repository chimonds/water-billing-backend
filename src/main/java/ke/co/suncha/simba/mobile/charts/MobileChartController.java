package ke.co.suncha.simba.mobile.charts;

import ke.co.suncha.simba.mobile.MobileUser;
import ke.co.suncha.simba.mobile.request.RequestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by maitha.manyala on 9/7/17.
 */
@RestController
@RequestMapping(value = "/api/v1/mobile/charts")
public class MobileChartController {
    @Autowired
    MobileChartManager mobileChartManager;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public RequestResponse getCharts(@RequestBody MobileUser mobileUser, HttpServletRequest request, HttpServletResponse response) {
        return mobileChartManager.getCharts(mobileUser);
    }
}
