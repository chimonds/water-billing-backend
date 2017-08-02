package ke.co.suncha.simba.aqua.sms;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maitha.manyala on 7/27/17.
 */
@Service
public class SMSInboxServiceImpl implements SMSInboxService {
    @Autowired
    SMSInboxRepository smsInboxRepository;
    @Autowired
    EntityManager entityManager;

    @Override
    public Boolean create(SMSInbox smsInbox) {
        try {
            if (smsInbox.getText() != null) {
                smsInbox.setText(smsInbox.getText().toLowerCase());
            }
            smsInbox = smsInboxRepository.save(smsInbox);
            return Boolean.TRUE;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Boolean.FALSE;
    }

    @Override
    public SMSInbox update(SMSInbox smsInbox) {
        return smsInboxRepository.save(smsInbox);
    }

    @Override
    public SMSInbox get(Long smsId) {
        return smsInboxRepository.findOne(smsId);
    }

    @Override
    public List<Long> getPending() {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QSMSInbox.sMSInbox.replied.eq(Boolean.FALSE));

        JPAQuery query = new JPAQuery(entityManager);
        List<Long> smsIdsList = query.from(QSMSInbox.sMSInbox).where(builder).orderBy(QSMSInbox.sMSInbox.smsInquiryId.asc()).list(QSMSInbox.sMSInbox.smsInquiryId);
        if (smsIdsList == null) {
            smsIdsList = new ArrayList<>();
        }
        return smsIdsList;
    }
}
