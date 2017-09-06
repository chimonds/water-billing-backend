package ke.co.suncha.simba.aqua.toActivate;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.admin.version.ReleaseManager;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.services.AccountManagerService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

/**
 * Created by maitha.manyala on 6/6/17.
 */
@Service
public class ToActivateService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ToActivateRepository toActivateRepository;

    @Autowired
    AccountManagerService accountService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    ReleaseManager releaseManager;

    public void create(Long accountId) {
        Account account = accountService.getByAccountId(accountId);
        if (account != null) {

            if (account.getIsCutOff() == Boolean.TRUE) {
                if (account.getOutstandingBalance() <= 0) {

                    //Check if account was added within the month

                    DateTime today = new DateTime();
                    DateTime startOfMonth = today.dayOfMonth().withMinimumValue().hourOfDay().withMinimumValue();
                    DateTime endOfMonth = today.dayOfMonth().withMaximumValue().hourOfDay().withMaximumValue();

                    BooleanBuilder builder = new BooleanBuilder();
                    builder.and(QToActivate.toActivate.account.accountId.eq(accountId));
                    builder.and(QToActivate.toActivate.transactionDate.goe(startOfMonth));
                    builder.and(QToActivate.toActivate.transactionDate.loe(endOfMonth));

                    JPAQuery query = new JPAQuery(entityManager);
                    Long count = query.from(QToActivate.toActivate).where(builder).singleResult(QToActivate.toActivate.count());
                    if (count != null) {
                        if (count == 0) {
                            ToActivate ta = new ToActivate();
                            ta.setAccount(account);
                            ta.setBalance(account.getOutstandingBalance());
                            ta.setTransactionDate(new DateTime());
                            ta = toActivateRepository.save(ta);
                        }
                    }
                }
            }
        }
    }

    @PostConstruct
    public void addPermissions() {
        releaseManager.addPermission("report_accounts_to_activate");
    }
}
