package com.ihome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电费缴费记录实体
 */
@Data
@TableName("electricity_payments")
public class ElectricityPayment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 电费账单ID
     */
    private Long billId;
    
    /**
     * 学生ID
     */
    private String studentId;
    
    /**
     * 宿舍ID
     */
    private String dormitoryId;
    
    /**
     * 缴费金额
     */
    private BigDecimal paymentAmount;
    
    /**
     * 缴费方式 (支付宝/微信/银行卡)
     */
    private String paymentMethod;
    
    /**
     * 缴费状态 (待支付/已支付/已退款)
     */
    private String paymentStatus;
    
    /**
     * 第三方交易号
     */
    private String transactionId;
    
    /**
     * 缴费时间
     */
    private LocalDateTime paymentTime;
    
    /**
     * 备注
     */
    private String remarks;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

