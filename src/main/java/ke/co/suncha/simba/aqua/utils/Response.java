package ke.co.suncha.simba.aqua.utils;

/**
 * Created by maitha.manyala on 10/18/17.
 */
public class Response {
    private Boolean error = Boolean.TRUE;
    private String message;

    public static Response ErrorOccurred(String message) {
        Response response = new Response();
        response.setError(Boolean.TRUE);
        response.setMessage(message);
        return response;
    }

    public static Response NoError() {
        Response response = new Response();
        response.setError(Boolean.FALSE);
        response.setMessage("");
        return response;
    }

    public Boolean hasError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}