package ke.co.suncha.simba.mobile.account;

import ke.co.suncha.simba.mobile.MobileUser;

/**
 * Created by maitha.manyala on 8/15/17.
 */
public class AccountPageRequest {
    MobileUser user;
    Integer page;

    public MobileUser getUser() {
        return user;
    }

    public void setUser(MobileUser user) {
        this.user = user;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}
