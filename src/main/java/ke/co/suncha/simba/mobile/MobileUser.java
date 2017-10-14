package ke.co.suncha.simba.mobile;

/**
 * Created by maitha.manyala on 8/10/17.
 */
public class MobileUser {
    private Long userId;
    private String password;
    private String name;
    private String email;
    private Integer organizationLevel = 0;
    private Integer ReadOnlyMeteredAccounts = 1;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getOrganizationLevel() {
        return organizationLevel;
    }

    public void setOrganizationLevel(Integer organizationLevel) {
        this.organizationLevel = organizationLevel;
    }

    public Integer getReadOnlyMeteredAccounts() {
        return ReadOnlyMeteredAccounts;
    }

    public void setReadOnlyMeteredAccounts(Integer readOnlyMeteredAccounts) {
        ReadOnlyMeteredAccounts = readOnlyMeteredAccounts;
    }
}
