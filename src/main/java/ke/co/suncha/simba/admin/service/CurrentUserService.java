package ke.co.suncha.simba.admin.service;

import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

/**
 * Created by manyala on 5/12/15.
 */

@Service
public class CurrentUserService  {
    private Long currentUserId = 2L;

    @Autowired
    private UserRepository userRepository;


    public  User getCurrent(){
        return userRepository.findOne(currentUserId);
    }

    public void setUser(Long userId) {
        this.currentUserId = userId;
    }
}
