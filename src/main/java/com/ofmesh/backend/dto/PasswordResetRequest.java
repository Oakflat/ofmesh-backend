package com.ofmesh.backend.dto;

public class PasswordResetRequest {
    private String email;
    private String code;       // 验证码
    private String newPassword;

    // === 手动 Getter / Setter ===
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}