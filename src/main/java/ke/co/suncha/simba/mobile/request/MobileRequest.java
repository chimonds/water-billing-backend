package ke.co.suncha.simba.mobile.request;

/**
 * Created by maitha.manyala on 8/10/17.
 */
public class MobileRequest<T> {
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

