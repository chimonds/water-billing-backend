package ke.co.suncha.simba.aqua.api;

import com.wordnik.swagger.annotations.Api;
import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.models.MeterReading;
import ke.co.suncha.simba.aqua.services.MeterReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by maitha.manyala on 7/26/15.
 */
@RestController
@RequestMapping(value = "/api/v1/meterReadings")
@Api(value = "Meters", description = "Connection meters API")
public class MeterReadingsController extends AbstractRestHandler {
    @Autowired
    private MeterReadingService meterReadingService;

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    public RestResponse getAll(@RequestBody RestRequestObject<RestPageRequest> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return meterReadingService.getAllByFilter(requestObject);
    }


    @RequestMapping(value = "/getImage", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    public RestResponse getMeterReadingImageString(@RequestBody RestRequestObject<MeterReading> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return meterReadingService.getMeterReadingImageString(requestObject);
    }
}
