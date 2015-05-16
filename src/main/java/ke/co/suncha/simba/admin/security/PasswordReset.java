package ke.co.suncha.simba.admin.security;

/**
 * Created by manyala on 5/14/15.
 */
public class PasswordReset {
    private String existingPassword;
    private String newPassword;
    private String confirmPassword;

    public String getExistingPassword() {
        return existingPassword;
    }

    public void setExistingPassword(String existingPassword) {
        this.existingPassword = existingPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
