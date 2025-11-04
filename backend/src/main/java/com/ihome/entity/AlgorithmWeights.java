package com.ihome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 算法权重配置实体
 */
@Data
@TableName("algorithm_weights")
public class AlgorithmWeights {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 权重类型 (QUESTIONNAIRE, TAG, MAJOR, GENDER, BED_TYPE)
     */
    private String weightType;
    
    /**
     * 权重值 (0.0-1.0)
     */
    private Double weightValue;
    
    /**
     * 权重描述
     */
    private String description;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdated;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

