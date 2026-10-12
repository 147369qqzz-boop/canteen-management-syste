package com.example.dto;

// 登录请求数据传输对象，和前端传参一一对应
public class LoginDTO {
    private String username;
    private String password;
    private String role;

    // 无参构造（必须）
    public LoginDTO() {}

    // Getter和Setter方法（必须，否则Controller里调用getUsername会报错）
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}