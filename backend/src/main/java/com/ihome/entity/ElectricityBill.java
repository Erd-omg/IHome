package com.ihome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电费账单实体
 */
@Data
@TableName("electricity_bills")
public class ElectricityBill {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 宿舍ID
     */
    private String dormitoryId;
    
    /**
     * 账单月份 (格式: YYYY-MM)
     */
    private String billMonth;
    
    /**
     * 用电量 (度)
     */
    private BigDecimal electricityUsage;
    
    /**
     * 电费单价 (元/度)
     */
    private BigDecimal unitPrice;
    
    /**
     * 总电费金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 当前余额
     */
    private BigDecimal currentBalance;
    
    /**
     * 账单状态 (未缴费/已缴费/逾期)
     */
    private String status;
    
    /**
     * 缴费截止日期
     */
    private LocalDateTime dueDate;
    
    /**
     * 缴费时间
     */
    private LocalDateTime paymentTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

