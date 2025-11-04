package com.ihome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分配反馈实体
 */
@Data
@TableName("allocation_feedback")
public class AllocationFeedback {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID
     */
    private String studentId;
    
    /**
     * 分配ID
     */
    private Long allocationId;
    
    /**
     * 室友满意度 (1-5分)
     */
    private Integer roommateSatisfaction;
    
    /**
     * 宿舍环境满意度 (1-5分)
     */
    private Integer environmentSatisfaction;
    
    /**
     * 整体满意度 (1-5分)
     */
    private Integer overallSatisfaction;
    
    /**
     * 反馈内容
     */
    private String feedbackContent;
    
    /**
     * 建议改进
     */
    private String suggestions;
    
    /**
     * 是否愿意调换
     */
    private Boolean willingToSwitch;
    
    /**
     * 反馈时间
     */
    private LocalDateTime feedbackTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

