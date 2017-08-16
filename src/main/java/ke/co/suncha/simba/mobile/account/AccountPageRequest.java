package ke.co.suncha.simba.mobile.account;

import ke.co.suncha.simba.mobile.MobileUser;

/**
 * Created by maitha.manyala on 8/15/17.
 */
public class AccountPageRequest {
    MobileUser mobileUser;
    Integer page;

    public MobileUser getMobileUser() {
        return mobileUser;
    }

    public void setMobileUser(MobileUser mobileUser) {
        this.mobileUser = mobileUser;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}
