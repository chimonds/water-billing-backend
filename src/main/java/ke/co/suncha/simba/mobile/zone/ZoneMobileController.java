package ke.co.suncha.simba.mobile.zone;

import ke.co.suncha.simba.mobile.MobileUser;
import ke.co.suncha.simba.mobile.request.RequestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by maitha.manyala on 8/15/17.
 */
@RestController
@RequestMapping(value = "/api/v1/mobile/zones")
public class ZoneMobileController {
    @Autowired
    ZoneMobileService zoneMobileService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public RequestResponse getAll(@RequestBody MobileUser mobileUser, HttpServletRequest request, HttpServletResponse response) {
        return zoneMobileService.getAll(mobileUser);
    }
}
