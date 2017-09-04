package ke.co.suncha.simba.mobile.upload;

import ke.co.suncha.simba.mobile.request.RequestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by maitha.manyala on 9/3/17.
 */
@RestController
@RequestMapping(value = "/api/v1/mobile/meterReadings")
public class MeterReadingController {
    @Autowired
    MeterReadingManager meterReadingManager;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public RequestResponse addRecord(@RequestBody UploadRequest uploadRequest, HttpServletRequest request, HttpServletResponse response) {
        return meterReadingManager.addMeterReadingRequest(uploadRequest);
//        @RequestParam MultipartFile image
    }
}
