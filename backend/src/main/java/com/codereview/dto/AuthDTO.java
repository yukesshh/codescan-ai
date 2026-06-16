package com.codereview.dto;

import jakarta.validation.constraints.*;

public class AuthDTO {

    public static class RegisterRequest {
        @NotBlank private String username;
        @NotBlank @Email private String email;
        @NotBlank @Size(min = 6) private String password;
        private String fullName;

        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getFullName() { return fullName; }
        public void setUsername(String v) { this.username = v; }
        public void setEmail(String v) { this.email = v; }
        public void setPassword(String v) { this.password = v; }
        public void setFullName(String v) { this.fullName = v; }
    }

    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;

        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public void setUsername(String v) { this.username = v; }
        public void setPassword(String v) { this.password = v; }
    }

    public static class AuthResponse {
        private String token;
        private String username;
        private String email;
        private String fullName;
        private Long userId;

        public String getToken() { return token; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public Long getUserId() { return userId; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final AuthResponse r = new AuthResponse();
            public Builder token(String v) { r.token = v; return this; }
            public Builder username(String v) { r.username = v; return this; }
            public Builder email(String v) { r.email = v; return this; }
            public Builder fullName(String v) { r.fullName = v; return this; }
            public Builder userId(Long v) { r.userId = v; return this; }
            public AuthResponse build() { return r; }
        }
    }
}