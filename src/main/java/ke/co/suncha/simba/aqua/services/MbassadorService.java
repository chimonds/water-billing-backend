package ke.co.suncha.simba.aqua.services;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;
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

    public MBassador bus = new MBassador();

    @Autowired
    AccountService accountService;

    public MbassadorService() {
        bus.subscribe(this);
    }

    @Handler
    public void updateAccountAgeing(String accountNo) {
        //log.info("Notified engine to update ageing for "+ accountNo);
        accountService.updateAccountAgeing(accountNo);
    }

//    @Handler
//    public void allocateMPESATransaction(MPESATransaction mpesaTransaction) {
//        mpesaService.allocateMPESATransaction(mpesaTransaction);
//    }
}
