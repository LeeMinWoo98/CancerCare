package org.example.form;

public class PasswordChangeForm {
    private String currentPassword; // 현재 비밀번호
    private String newPassword; // 새 비밀번호
    private String confirmPassword; // 비밀번호 확인

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
