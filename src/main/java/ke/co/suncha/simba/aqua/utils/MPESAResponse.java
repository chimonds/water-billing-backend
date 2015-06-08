package ke.co.suncha.simba.aqua.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ke.co.suncha.simba.aqua.models.MPESATransaction;

import java.util.List;

/**
 * Created by manyala on 6/6/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MPESAResponse {
    private String message;
    private Boolean error;
    private List<MPESATransaction> payload;


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

    public List<MPESATransaction> getPayload() {
        return payload;
    }

    public void setPayload(List<MPESATransaction> payload) {
        this.payload = payload;
    }
}
