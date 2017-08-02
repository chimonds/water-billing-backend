package ke.co.suncha.simba.aqua.sms;

import java.util.List;

/**
 * Created by maitha.manyala on 7/27/17.
 */
public interface SMSInboxService {
    Boolean create(SMSInbox smsInbox);

    SMSInbox update(SMSInbox smsInbox);

    SMSInbox get(Long smsId);

    List<Long> getPending();
}
