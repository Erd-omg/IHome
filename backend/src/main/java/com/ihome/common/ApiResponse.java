package com.ihome.common;

// 删除了所有 Lombok 相关的导入

public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;
    private Boolean success;

    // 添加了全参构造函数
    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = (code != null && code == 0);
    }

    // 添加了无参构造函数
    public ApiResponse() {
    }

    // 静态工厂方法：成功，带数据
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    // 静态工厂方法：成功，不带数据
    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(0, "success", null);
    }

    // 静态工厂方法：失败
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(-1, message, null);
    }

    // Getter 和 Setter 方法
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}