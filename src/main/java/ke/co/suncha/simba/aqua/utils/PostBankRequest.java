package ke.co.suncha.simba.aqua.utils;

/**
 * Created by manyala on 6/11/15.
 */
public class PostBankRequest {
    private String username = "";
    private String password = "";
    private String recordId;

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
