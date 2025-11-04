package com.ihome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电费提醒设置实体
 */
@Data
@TableName("electricity_reminders")
public class ElectricityReminder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID
     */
    private String studentId;
    
    /**
     * 宿舍ID
     */
    private String dormitoryId;
    
    /**
     * 余额提醒阈值 (元)
     */
    private BigDecimal balanceThreshold;
    
    /**
     * 是否启用余额提醒
     */
    private Boolean balanceReminderEnabled;
    
    /**
     * 缴费截止提醒天数
     */
    private Integer dueDateReminderDays;
    
    /**
     * 是否启用截止日期提醒
     */
    private Boolean dueDateReminderEnabled;
    
    /**
     * 提醒方式 (站内信/短信/邮件)
     */
    private String reminderMethod;
    
    /**
     * 最后提醒时间
     */
    private LocalDateTime lastReminderTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

