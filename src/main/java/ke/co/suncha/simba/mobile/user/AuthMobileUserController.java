package ke.co.suncha.simba.mobile.user;

import ke.co.suncha.simba.mobile.MobileUser;
import ke.co.suncha.simba.mobile.request.RequestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by maitha.manyala on 8/10/17.
 */
@RestController
@RequestMapping(value = "/api/v1/mobile/auth")
public class AuthMobileUserController {
    @Autowired
    AuthenticateMobileUserService authenticateMobileUserService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public RequestResponse auth(@RequestBody MobileUser mobileUser, HttpServletRequest request, HttpServletResponse response) {
        return authenticateMobileUserService.login(mobileUser);
    }
}
