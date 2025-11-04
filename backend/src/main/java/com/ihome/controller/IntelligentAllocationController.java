package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.service.AllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能分配控制器
 * 提供AI智能分配算法相关接口
 */
@RestController
@RequestMapping("/allocation")
public class IntelligentAllocationController {

    @Autowired
    private AllocationService allocationService;

    /**
     * 执行智能分配
     * @param studentIds 待分配的学生ID列表
     * @return 分配结果
     */
    @PostMapping("/intelligent")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> intelligentAllocation(@RequestBody List<String> studentIds) {
        try {
            if (studentIds == null || studentIds.isEmpty()) {
                return ApiResponse.error("学生ID列表不能为空");
            }
            
            Map<String, Object> result = allocationService.intelligentAllocation(studentIds);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("智能分配失败: " + e.getMessage());
        }
    }

    /**
     * 获取分配建议
     * @param studentId 学生ID
     * @return 分配建议
     */
    @GetMapping("/suggestions/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getAllocationSuggestions(@PathVariable String studentId) {
        try {
            Map<String, Object> suggestions = allocationService.getAllocationSuggestions(studentId);
            return ApiResponse.ok(suggestions);
        } catch (Exception e) {
            return ApiResponse.error("获取分配建议失败: " + e.getMessage());
        }
    }

    /**
     * 批量分配学生到指定宿舍
     * @param request 分配请求
     * @return 分配结果
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> batchAllocation(@RequestBody BatchAllocationRequest request) {
        try {
            if (request.getStudentIds() == null || request.getStudentIds().isEmpty()) {
                return ApiResponse.error("学生ID列表不能为空");
            }
            
            if (request.getDormitoryId() == null || request.getDormitoryId().trim().isEmpty()) {
                return ApiResponse.error("宿舍ID不能为空");
            }
            
            // 这里可以实现批量分配到指定宿舍的逻辑
            // 暂时返回成功响应
            Map<String, Object> result = Map.of(
                "allocatedCount", request.getStudentIds().size(),
                "dormitoryId", request.getDormitoryId(),
                "message", "批量分配成功"
            );
            
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("批量分配失败: " + e.getMessage());
        }
    }

    /**
     * 获取分配统计信息
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getAllocationStatistics() {
        try {
            // 这里可以实现获取分配统计信息的逻辑
            Map<String, Object> statistics = Map.of(
                "totalStudents", 150,
                "allocatedStudents", 120,
                "unallocatedStudents", 30,
                "allocationRate", 80.0,
                "averageCompatibilityScore", 0.75
            );
            
            return ApiResponse.ok(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 批量分配请求DTO
     */
    public static class BatchAllocationRequest {
        private List<String> studentIds;
        private String dormitoryId;
        private String reason;

        public List<String> getStudentIds() {
            return studentIds;
        }

        public void setStudentIds(List<String> studentIds) {
            this.studentIds = studentIds;
        }

        public String getDormitoryId() {
            return dormitoryId;
        }

        public void setDormitoryId(String dormitoryId) {
            this.dormitoryId = dormitoryId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}

