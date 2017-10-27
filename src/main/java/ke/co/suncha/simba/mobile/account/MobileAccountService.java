package ke.co.suncha.simba.mobile.account;

import com.mysema.query.BooleanBuilder;
import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.admin.service.UserService;
import ke.co.suncha.simba.admin.utils.CustomPage;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.account.QAccount;
import ke.co.suncha.simba.aqua.billing.BillingService;
import ke.co.suncha.simba.aqua.options.SystemOptionService;
import ke.co.suncha.simba.aqua.reports.StatementRecord;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.scheme.zone.Zone;
import ke.co.suncha.simba.aqua.scheme.zone.ZoneService;
import ke.co.suncha.simba.aqua.scheme.zone.meterReader.MeterReaderService;
import ke.co.suncha.simba.aqua.services.AccountManagerService;
import ke.co.suncha.simba.mobile.request.RequestResponse;
import ke.co.suncha.simba.mobile.user.MobileUserAuthService;
import ke.co.suncha.simba.mobile.zone.MZone;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maitha.manyala on 8/15/17.
 */
@Service
public class MobileAccountService {
    @Autowired
    MobileUserAuthService mobileUserAuthService;

    @Autowired
    BillingService billingService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountManagerService accountService;

    @Autowired
    AuthManager authManager;

    @Autowired
    UserService userService;

    @Autowired
    ZoneService zoneService;

    @Autowired
    MeterReaderService meterReaderService;

    @Autowired
    SystemOptionService systemOptionService;

    @Autowired
    SimbaOptionService optionService;

    //private final Integer pageSize = 200;

    public Integer getPageSize() {
        Integer pageSize = 200;
        try {
            Integer p = Integer.valueOf(optionService.getOption("DEFAULT_PAGE_SIZE").getValue());
            if (p != null) {
                pageSize = p;
            }
        } catch (Exception ex) {

        }
        return pageSize;
    }

    public RequestResponse<CustomPage> getPage(AccountPageRequest accountPageRequest) {

        Integer page = accountPageRequest.getPage();

        RequestResponse<CustomPage> response = new RequestResponse<>();
        CustomPage customPage = new CustomPage();

        if (!mobileUserAuthService.canLogin(accountPageRequest.user)) {
            response.setError(Boolean.TRUE);
            response.setMessage("Access denied to this resource");
        } else if (!systemOptionService.isMobileEnabled()) {
            response.setError(Boolean.TRUE);
            response.setMessage(systemOptionService.getMobileLicenceExpired());
        } else {

            User user = userService.getByEmailAddress(accountPageRequest.getUser().getEmail());
            Boolean organizationLevel = authManager.hasPermission(user.getUserId(), "mobile_OrganizationLevel");

            BooleanBuilder builder = new BooleanBuilder();
            if (!organizationLevel) {
                List<Zone> zoneList = meterReaderService.getZonesWithMeterReader(user.getUserId());
                if (zoneList != null) {
                    builder.and(QAccount.account.zone.in(zoneList));
                }
            }

            Page<Account> accountPage = accountRepository.findAll(builder, new PageRequest(page, getPageSize()));
            if (accountPage != null) {
                if (accountPage.hasContent()) {

                    customPage.setTotalPages(accountPage.getTotalPages());
                    customPage.setTotalElements(accountPage.getTotalElements());
                    customPage.setLast(accountPage.isLast());
                    customPage.setHasNext(accountPage.hasNext());

                    List<MobileAccount> mobileAccounts = new ArrayList<>();
                    for (Account account : accountPage.getContent()) {
                        MobileAccount mobileAccount = new MobileAccount();
                        mobileAccount.setAccountId(account.getAccountId());
                        mobileAccount.setName(account.getAccName());
                        mobileAccount.setBalance(account.getOutstandingBalance());
                        mobileAccount.setAccNo(account.getAccNo());
                        if (StringUtils.isNotEmpty(account.getPhoneNumber())) {
                            mobileAccount.setPhoneNo(account.getPhoneNumber());
                        }

                        if (StringUtils.isEmpty(mobileAccount.getPhoneNo())) {
                            if (StringUtils.isNotEmpty(account.getConsumer().getPhoneNumber())) {
                                mobileAccount.setPhoneNo(account.getConsumer().getPhoneNumber());
                            }
                        }

                        if (account.getIsCutOff()) {
                            mobileAccount.setActive(0);
                        } else {
                            mobileAccount.setActive(1);
                        }
                        if (account.getMeter() != null) {
                            mobileAccount.setHasMeter(1);
                        }
                        MZone mZone = new MZone();
                        mZone.setZoneId(account.getZone().getZoneId());
                        mobileAccount.setZone(mZone);

                        mobileAccount.setCurrentMeterReading(billingService.getLastMeterReading(account.getAccountId()));

                        mobileAccounts.add(mobileAccount);
                    }

                    customPage.setContent(mobileAccounts);
                }
            }
        }

        response.setError(Boolean.FALSE);
        response.setMessage("Data fetched successfully");
        response.setObject(customPage);
        return response;
    }

    public RequestResponse<MobileStatement> getStatement(StatementRequest statementRequest) {
        RequestResponse<MobileStatement> response = new RequestResponse<>();
        if (!mobileUserAuthService.canLogin(statementRequest.getUser())) {
            response.setError(Boolean.TRUE);
            response.setMessage("Access denied to this resource");
        } else if (!systemOptionService.isMobileEnabled()) {
            response.setError(Boolean.TRUE);
            response.setMessage(systemOptionService.getMobileLicenceExpired());
        } else {

            if (statementRequest.getAccountId() == null) {
                response.setError(Boolean.TRUE);
                response.setMessage("Invalid account resource");
            } else {
                Account account = accountService.getByAccountId(statementRequest.getAccountId());
                if (account == null) {
                    response.setError(Boolean.TRUE);
                    response.setMessage("Invalid account resource");
                } else {
                    List<StatementRecord> statementRecords = accountService.getStatementRecords(account.getAccountId());
                    MobileStatement statement = new MobileStatement();
                    statement.setRecords(statementRecords);

                    MobileAccount mobileAccount = new MobileAccount();
                    mobileAccount.setAccNo(account.getAccNo());
                    mobileAccount.setBalance(account.getOutstandingBalance());
                    mobileAccount.setAccountId(account.getAccountId());
                    mobileAccount.setName(account.getAccName());
                    mobileAccount.setCurrentMeterReading(billingService.getLastMeterReading(account.getAccountId()));

                    if (account.getIsCutOff()) {
                        mobileAccount.setActive(0);
                    } else {
                        mobileAccount.setActive(1);
                    }

                    if (StringUtils.isNotEmpty(account.getPhoneNumber())) {
                        mobileAccount.setPhoneNo(account.getPhoneNumber());
                    }

                    if (StringUtils.isEmpty(mobileAccount.getPhoneNo())) {
                        if (StringUtils.isNotEmpty(account.getConsumer().getPhoneNumber())) {
                            mobileAccount.setPhoneNo(account.getConsumer().getPhoneNumber());
                        }
                    }

                    if (account.getMeter() != null) {
                        mobileAccount.setHasMeter(1);
                    }

                    statement.setAccount(mobileAccount);
                    response.setObject(statement);
                    response.setError(Boolean.FALSE);
                    response.setMessage("Fetched data successfully");
                }
            }
        }
        return response;
    }
}