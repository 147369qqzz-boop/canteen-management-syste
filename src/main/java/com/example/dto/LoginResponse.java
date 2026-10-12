package com.example.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class LoginResponse {

    private String token;
    private Map<String, Object> user;

    public LoginResponse(String token, Map<String, Object> user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Map<String, Object> getUser() {
        return user;
    }

    public void setUser(Map<String, Object> user) {
        this.user = user;
    }

    public static LoginResponse of(String token, Integer id, String username, String name,
                                    String role, String avatar) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", id);
        user.put("username", username);
        user.put("name", name);
        user.put("role", role);
        user.put("avatar", avatar);
        return new LoginResponse(token, user);
    }
}
