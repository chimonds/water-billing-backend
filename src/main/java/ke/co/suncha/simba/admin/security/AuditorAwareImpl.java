package ke.co.suncha.simba.admin.security;

import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;

/**
 * Created by manyala on 5/12/15.
 */
public class AuditorAwareImpl implements AuditorAware<User> {
    @Autowired
    private CurrentUserService currentUserService;

    @Override
    public User getCurrentAuditor() {
        return currentUserService.getCurrent();
    }
}
