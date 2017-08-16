package ke.co.suncha.simba.mobile.user;

import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.UserService;
import ke.co.suncha.simba.mobile.MobileUser;
import ke.co.suncha.simba.mobile.request.RequestResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by maitha.manyala on 8/10/17.
 */
@Service
public class MobileUserAuthService {
    @Autowired
    UserService userService;

    @Autowired
    AuthManager authManager;

    public Boolean canLogin(MobileUser mobileUser) {
        if (mobileUser == null) {
            return Boolean.FALSE;
        }

        if (StringUtils.isEmpty(mobileUser.getEmail()) || StringUtils.isEmpty(mobileUser.getPassword())) {
            return Boolean.FALSE;
        }

        User user = userService.getByEmailAddress(mobileUser.getEmail());

        if (user == null) {
            return Boolean.FALSE;
        }

        if (!authManager.passwordValid(user, mobileUser.getPassword())) {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    public RequestResponse<MobileUser> login(MobileUser mobileUser) {
        RequestResponse<MobileUser> response = new RequestResponse<>();
        response.setError(Boolean.TRUE);
        response.setMessage("Invalid username/password");

        if (!canLogin(mobileUser)) {
            return response;
        }

        User user = userService.getByEmailAddress(mobileUser.getEmail());

        mobileUser.setName(user.getFirstName() + " " + user.getLastName());
        mobileUser.setUserId(user.getUserId());

        response.setError(Boolean.FALSE);
        response.setMessage("Logged in");
        response.setObject(mobileUser);
        return response;
    }
}
