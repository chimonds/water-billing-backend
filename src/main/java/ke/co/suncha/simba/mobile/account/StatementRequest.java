package ke.co.suncha.simba.mobile.account;

import ke.co.suncha.simba.mobile.MobileUser;

/**
 * Created by maitha.manyala on 8/20/17.
 */
public class StatementRequest {
    MobileUser user;
    Long accountId;

    public MobileUser getUser() {
        return user;
    }

    public void setUser(MobileUser user) {
        this.user = user;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
