package ke.co.suncha.simba.aqua.utils;

import ke.co.suncha.simba.aqua.models.AccountSummary;

import java.util.List;

/**
 * Created by maitha.manyala on 7/10/15.
 */
public class AccountSummaryRequest extends MobileClientRequest {
    private AccountSummary payload;

    public AccountSummary getAccountSummary() {
        return payload;
    }

    public void setAccountSummary(AccountSummary accountSummary) {
        this.payload = accountSummary;
    }
}
