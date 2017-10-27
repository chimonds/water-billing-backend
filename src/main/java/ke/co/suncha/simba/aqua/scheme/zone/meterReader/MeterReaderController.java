package ke.co.suncha.simba.aqua.scheme.zone.meterReader;

import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by maitha.manyala on 8/30/17.
 */
@RestController
@RequestMapping(value = "/api/v1/meterReaders")
public class MeterReaderController {
    @Autowired
    private MeterReaderManager meterReaderManager;

    @RequestMapping(value = "/addMeterReaderToZone", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse addMeterReaderToZone(@RequestBody RestRequestObject<MeterReader> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return this.meterReaderManager.addMeterReaderToZone(requestObject);
    }

    @RequestMapping(value = "/removeMeterReaderFromZone", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse removeMeterReaderFromZone(@RequestBody RestRequestObject<MeterReader> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return this.meterReaderManager.removeMeterReaderFromZone(requestObject);
    }

    @RequestMapping(value = "/getZoneMeterReaders", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse getZoneMeterReaders(@RequestBody RestRequestObject<MeterReader> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return this.meterReaderManager.getZoneMeterReaders(requestObject);
    }


    @RequestMapping(value = "/getMeterReaders", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse getMeterReaders(@RequestBody RestRequestObject<MeterReader> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return this.meterReaderManager.getMeterReaders(requestObject);
    }

    @RequestMapping(value = "/getMeterReadersNotInZone", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse getMeterReadersNotInZone(@RequestBody RestRequestObject<MeterReader> requestObject, HttpServletRequest request, HttpServletResponse response) {
        return this.meterReaderManager.getMeterReadersNotInZone(requestObject);
    }
}
