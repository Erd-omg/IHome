package com.ihome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 消息通知实体类
 */
@TableName("notifications")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    // 接收者ID（学生ID或管理员ID）
    private String receiverId;
    
    // 接收者类型（student/admin）
    private String receiverType;
    
    // 通知标题
    private String title;
    
    // 通知内容
    private String content;
    
    // 通知类型（system/repair/payment/switch/allocation）
    private String type;
    
    // 通知优先级（low/normal/high/urgent）
    private String priority;
    
    // 是否已读
    private Boolean isRead;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 阅读时间
    private LocalDateTime readTime;
    
    // 过期时间
    private LocalDateTime expireTime;
    
    // 相关业务ID（如维修工单ID、调换申请ID等）
    private String relatedId;
    
    // 相关业务类型
    private String relatedType;
    
    // 发送者ID（可选）
    private String senderId;
    
    // 发送者类型（可选）
    private String senderType;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public String getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId;
    }

    public String getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }
}

