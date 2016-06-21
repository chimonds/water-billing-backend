package ke.co.suncha.simba.aqua.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by maitha.manyala on 6/19/16.
 */
@Service
public class MbassadorService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    //public MBassador<Long> bus = new MBassador<Long>();

    @Autowired
    AccountService accountService;

//    public MbassadorService() {
//        bus.subscribe(this);
//    }

//    @Handler
//    public void updateBalance(Long accountId) {
//        accountService.updateBalance(accountId);
//    }

}
