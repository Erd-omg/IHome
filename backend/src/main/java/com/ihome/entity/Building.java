package com.ihome.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("buildings")
public class Building {
    @TableId
    private String id;
    private String buildingNumber;
    private String buildingName;
    private String genderType;
    private Integer floorCount;
    private Integer roomCount;
    private String description;
}


