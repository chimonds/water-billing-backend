package ke.co.suncha.simba.mobile.account;

import ke.co.suncha.simba.admin.utils.CustomPage;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.mobile.request.RequestResponse;
import ke.co.suncha.simba.mobile.user.MobileUserAuthService;
import ke.co.suncha.simba.mobile.zone.MZone;
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
    AccountRepository accountRepository;

    private final Integer pageSize = 25;

    public RequestResponse<CustomPage> getPage(AccountPageRequest accountPageRequest) {

        Integer page = 0;
        if (accountPageRequest != null) {
            if (accountPageRequest.getPage() != null) {
                page = accountPageRequest.getPage() - 1;
            }
        }

        RequestResponse<CustomPage> response = new RequestResponse<>();
        CustomPage customPage = new CustomPage();

        if (!mobileUserAuthService.canLogin(accountPageRequest.mobileUser)) {
            response.setError(Boolean.TRUE);
            response.setMessage("Access denied to this resource");
        } else {
            Page<Account> accountPage = accountRepository.findAll(new PageRequest(page, pageSize));
            if (page != null) {
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

                        MZone mZone = new MZone();
                        mZone.setZoneId(account.getZone().getZoneId());
                        mobileAccount.setZone(mZone);

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
}