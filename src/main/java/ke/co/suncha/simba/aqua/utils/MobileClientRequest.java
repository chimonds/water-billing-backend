package ke.co.suncha.simba.aqua.utils;

import ke.co.suncha.simba.admin.security.Credential;

/**
 * Created by maitha.manyala on 7/10/15.
 */
public class MobileClientRequest {
    private Credential login;
    private Object payload;

    public Credential getLogin() {
        return login;
    }

    public void setLogin(Credential login) {
        this.login = login;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
