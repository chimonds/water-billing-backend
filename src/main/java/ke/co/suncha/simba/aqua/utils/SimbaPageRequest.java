package ke.co.suncha.simba.aqua.utils;

/**
 * Created by maitha.manyala on 6/21/16.
 */
public class SimbaPageRequest<T> {
    private Integer size = 10;
    private Integer page = 0;
    private T object;

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
