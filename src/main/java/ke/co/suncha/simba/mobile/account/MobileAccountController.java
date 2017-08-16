package ke.co.suncha.simba.mobile.account;

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
@RequestMapping(value = "/api/v1/mobile/accounts")
public class MobileAccountController {
    @Autowired
    MobileAccountService mobileAccountService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public RequestResponse getAll(@RequestBody AccountPageRequest accountPageRequest, HttpServletRequest request, HttpServletResponse response) {
        return mobileAccountService.getPage(accountPageRequest);
    }
}
