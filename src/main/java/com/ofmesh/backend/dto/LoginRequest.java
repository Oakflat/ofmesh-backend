package com.ofmesh.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequest {

    // 统一入口：identifier
    private String identifier;
    private String password;

    public String getLoginKey() {
        return identifier;
    }

    // 兼容：前端发 identifier
    @JsonProperty("identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    // 兼容：前端发 username
    @JsonProperty("username")
    public void setUsername(String username) {
        if (this.identifier == null || this.identifier.isBlank()) {
            this.identifier = username;
        }
    }

    // 兼容：前端发 email
    @JsonProperty("email")
    public void setEmail(String email) {
        if (this.identifier == null || this.identifier.isBlank()) {
            this.identifier = email;
        }
    }

    // password
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // （可选）为了兼容旧代码里 request.getUsername()
    public String getUsername() { return identifier; }
}
