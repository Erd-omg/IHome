package com.ihome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 宿舍调换申请实体类
 */
@TableName("dormitory_switches")
public class DormitorySwitch {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    // 申请人学号
    private String applicantId;
    
    // 目标室友学号（可选，如果指定了具体室友）
    private String targetStudentId;
    
    // 当前床位ID
    private String currentBedId;
    
    // 目标床位ID（可选，如果指定了具体床位）
    private String targetBedId;
    
    // 调换原因
    private String reason;
    
    // 申请状态：待审核、已通过、已拒绝、已完成
    private String status;
    
    // 申请时间
    private LocalDateTime applyTime;
    
    // 审核时间
    private LocalDateTime reviewTime;
    
    // 审核人（管理员ID）
    private String reviewerId;
    
    // 审核意见
    private String reviewComment;
    
    // 完成时间
    private LocalDateTime completeTime;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    public String getTargetStudentId() {
        return targetStudentId;
    }

    public void setTargetStudentId(String targetStudentId) {
        this.targetStudentId = targetStudentId;
    }

    public String getCurrentBedId() {
        return currentBedId;
    }

    public void setCurrentBedId(String currentBedId) {
        this.currentBedId = currentBedId;
    }

    public String getTargetBedId() {
        return targetBedId;
    }

    public void setTargetBedId(String targetBedId) {
        this.targetBedId = targetBedId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public LocalDateTime getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }
}

