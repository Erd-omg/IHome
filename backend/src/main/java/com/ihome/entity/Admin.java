package com.ihome.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("admins")
public class Admin {
    @TableId
    private String id;
    private String name;
    private String phoneNumber;
    private String email;
    private String password;
    private String adminType;

    // Getter方法
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getAdminType() { return adminType; }

    // Setter方法
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setAdminType(String adminType) { this.adminType = adminType; }
}


