package com.example.utils;

// 通用返回结果工具类
public class Result<T> {
    private String code; // 状态码：200成功，其他为失败
    private String msg;  // 提示信息
    private T data;      // 返回的数据（泛型，支持任意类型）

    // 成功（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode("200");
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    // 成功（无数据）
    public static <T> Result<T> success() {
        Result<T> r = new Result<>();
        r.setCode("200");
        r.setMsg("操作成功");
        return r;
    }

    // 失败（自定义错误信息）
    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.setCode("500");
        r.setMsg(msg);
        return r;
    }

    // 失败（自定义状态码+信息）
    public static <T> Result<T> error(String code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    // Getter和Setter
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}