package ke.co.suncha.simba.aqua.utils;

import ke.co.suncha.simba.aqua.models.MPESATransaction;
import ke.co.suncha.simba.aqua.models.PostBankTransaction;

import java.util.List;

/**
 * Created by manyala on 6/11/15.
 */
public class PostBankResponse {
    private String message;
    private Boolean error;
    private List<PostBankTransaction> payload;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public List<PostBankTransaction> getPayload() {
        return payload;
    }

    public void setPayload(List<PostBankTransaction> payload) {
        this.payload = payload;
    }
}
