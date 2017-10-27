package ke.co.suncha.simba.aqua.api;

import com.wordnik.swagger.annotations.Api;
import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.aqua.models.MeterReading;
import ke.co.suncha.simba.aqua.services.MeterReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
        return meterReadingService.getPage(requestObject);
    }


    @RequestMapping(value = "/{meterReadingId}", method = RequestMethod.PUT, consumes = { "application/json"}, produces = { "application/json" })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public RestResponse update(@PathVariable("meterReadingId") Long meterReadingId, @RequestBody RestRequestObject<MeterReading> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return meterReadingService.update(requestObject, meterReadingId);
    }

    @RequestMapping(value = "/getImage", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    public RestResponse getMeterReadingImageString(@RequestBody RestRequestObject<MeterReading> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return meterReadingService.getMeterReadingImageString(requestObject);
    }
}
