package ke.co.suncha.simba.mobile.request;

/**
 * Created by maitha.manyala on 8/10/17.
 */
public class RequestResponse<T> {
    private Boolean error;
    private T object;
    private String message;

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

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}