package com.ihome.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ihome.common.ApiResponse;
import com.ihome.entity.RepairOrder;
import com.ihome.mapper.RepairOrderMapper;
import com.ihome.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/repairs")
public class RepairController {
    private final RepairOrderMapper repairMapper;
    
    @Autowired
    private NotificationService notificationService;

    public RepairController(RepairOrderMapper repairMapper) {
        this.repairMapper = repairMapper;
    }

    @PostMapping
    public ApiResponse<?> createRepairOrder(@RequestBody @Valid CreateRepairRequest req) {
        try {
            // 从JWT令牌中获取学生ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String studentId;
            if (authentication == null || authentication.getName() == null) {
                // 如果请求中有studentId字段，使用它（用于测试环境）
                if (req.getStudentId() != null && !req.getStudentId().isEmpty()) {
                    studentId = req.getStudentId();
                } else {
                    return ApiResponse.error("未获取到学生ID");
                }
            } else {
                studentId = authentication.getName();
            }
            
            RepairOrder repair = new RepairOrder();
            repair.setStudentId(studentId);
            repair.setDormitoryId(req.getDormitoryId());
            repair.setRepairType(req.getRepairType());
            repair.setDescription(req.getDescription());
            repair.setCreatedAt(LocalDateTime.now());
            repair.setStatus("待处理");
            // 确保urgencyLevel使用正确的枚举值
            String urgencyLevel = req.getUrgencyLevel();
            if ("紧急".equals(urgencyLevel) || "urgent".equalsIgnoreCase(urgencyLevel)) {
                repair.setUrgencyLevel("紧急");
            } else if ("不紧急".equals(urgencyLevel) || "low".equalsIgnoreCase(urgencyLevel)) {
                repair.setUrgencyLevel("不紧急");
            } else {
                repair.setUrgencyLevel("一般"); // 默认值
            }
            
            repairMapper.insert(repair);
            
            return ApiResponse.ok(repair);
        } catch (Exception e) {
            return ApiResponse.error("创建维修申请失败: " + e.getMessage());
        }
    }

    @GetMapping("/student/{studentId}")
    public ApiResponse<List<RepairOrder>> getStudentRepairs(@PathVariable String studentId,
                                                            @RequestParam(required = false) String status) {
        List<RepairOrder> repairs = repairMapper.selectList(
                Wrappers.<RepairOrder>lambdaQuery()
                        .eq(RepairOrder::getStudentId, studentId)
                        .eq(status != null, RepairOrder::getStatus, status)
                        .orderByDesc(RepairOrder::getCreatedAt)
        );
        return ApiResponse.ok(repairs);
    }

    @GetMapping
    public ApiResponse<List<RepairOrder>> searchRepairs(@RequestParam(required = false) String studentId,
                                                        @RequestParam(required = false) String dormitoryId,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(required = false) String repairType,
                                                        @RequestParam(required = false) String sort) {
        var queryWrapper = Wrappers.<RepairOrder>lambdaQuery();
        
        // 模糊搜索学号
        if (studentId != null && !studentId.isEmpty()) {
            queryWrapper.like(RepairOrder::getStudentId, studentId);
        }
        
        // 模糊搜索宿舍ID
        if (dormitoryId != null && !dormitoryId.isEmpty()) {
            queryWrapper.like(RepairOrder::getDormitoryId, dormitoryId);
        }
        
        // 精确匹配状态
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(RepairOrder::getStatus, status);
        }
        
        // 精确匹配维修类型
        if (repairType != null && !repairType.isEmpty()) {
            queryWrapper.eq(RepairOrder::getRepairType, repairType);
        }
        
        // 排序处理
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String sortField = sortParts[0];
                String sortOrder = sortParts[1].toLowerCase();
                
                if ("studentId".equals(sortField)) {
                    if ("asc".equals(sortOrder)) {
                        queryWrapper.orderByAsc(RepairOrder::getStudentId);
                    } else {
                        queryWrapper.orderByDesc(RepairOrder::getStudentId);
                    }
                } else if ("dormitoryId".equals(sortField)) {
                    if ("asc".equals(sortOrder)) {
                        queryWrapper.orderByAsc(RepairOrder::getDormitoryId);
                    } else {
                        queryWrapper.orderByDesc(RepairOrder::getDormitoryId);
                    }
                } else if ("createdAt".equals(sortField)) {
                    if ("asc".equals(sortOrder)) {
                        queryWrapper.orderByAsc(RepairOrder::getCreatedAt);
                    } else {
                        queryWrapper.orderByDesc(RepairOrder::getCreatedAt);
                    }
                }
            }
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc(RepairOrder::getCreatedAt);
        }
        
        List<RepairOrder> repairs = repairMapper.selectList(queryWrapper);
        return ApiResponse.ok(repairs);
    }

    @PutMapping("/{repairId}/status")
    public ApiResponse<?> updateRepairStatus(@PathVariable Integer repairId,
                                             @RequestParam String status,
                                             @RequestParam(required = false) String ignored) {
        RepairOrder repair = repairMapper.selectById(repairId);
        if (repair == null) {
            return ApiResponse.error("维修单不存在");
        }
        
        String oldStatus = repair.getStatus();
        repair.setStatus(status);
        // 使用UpdateWrapper更新，避免lambda缓存问题（在测试环境中）
        UpdateWrapper<RepairOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", repairId)
                     .set("status", status)
                     .set("updated_at", LocalDateTime.now());
        repairMapper.update(null, updateWrapper);
        
        // 发送状态更新通知给学生
        if (!oldStatus.equals(status)) {
            String title = "维修状态更新";
            String content = String.format("您的维修工单（工单号：%s）状态已更新为：%s", String.valueOf(repairId), status);
            String priority = "已完成".equals(status) ? "normal" : "high";
            notificationService.sendRepairNotification(
                    String.valueOf(repairId), 
                    repair.getStudentId(), 
                    title, 
                    content, 
                    priority
            );
        }
        
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<RepairOrder> getRepairOrder(@PathVariable Integer id) {
        RepairOrder repair = repairMapper.selectById(id);
        if (repair == null) {
            return ApiResponse.error("维修工单不存在");
        }
        return ApiResponse.ok(repair);
    }

    public static class CreateRepairRequest {
        private String studentId;  // 可选，用于测试环境
        @NotBlank
        private String dormitoryId;
        @NotBlank
        private String repairType;
        @NotBlank
        private String description;
        private String urgencyLevel = "一般";

        // Getters and Setters
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getDormitoryId() { return dormitoryId; }
        public void setDormitoryId(String dormitoryId) { this.dormitoryId = dormitoryId; }
        public String getRepairType() { return repairType; }
        public void setRepairType(String repairType) { this.repairType = repairType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getUrgencyLevel() { return urgencyLevel; }
        public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    }
}
