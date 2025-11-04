package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.common.OperationLog;
import com.ihome.entity.AllocationFeedback;
import com.ihome.service.AllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 分配反馈控制器
 */
@RestController
@RequestMapping("/allocation-feedback")
public class AllocationFeedbackController {
    
    @Autowired
    private AllocationService allocationService;
    
    /**
     * 提交分配反馈
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @OperationLog(module = "智能分配", operationType = "CREATE", description = "提交分配反馈")
    public ApiResponse<Map<String, Object>> submitFeedback(@RequestBody AllocationFeedback feedback) {
        Map<String, Object> result = allocationService.submitAllocationFeedback(feedback);
        
        if ((Boolean) result.get("success")) {
            return ApiResponse.ok(result);
        } else {
            return ApiResponse.error((String) result.get("message"));
        }
    }
    
    /**
     * 获取学生反馈记录
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @OperationLog(module = "智能分配", operationType = "QUERY", description = "获取学生反馈记录")
    public ApiResponse<List<AllocationFeedback>> getStudentFeedback(@PathVariable String studentId) {
        List<AllocationFeedback> feedbacks = allocationService.getStudentFeedback(studentId);
        return ApiResponse.ok(feedbacks);
    }
    
    /**
     * 获取分配效果统计（管理员）
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "智能分配", operationType = "QUERY", description = "获取分配效果统计")
    public ApiResponse<Map<String, Object>> getAllocationStatistics() {
        Map<String, Object> statistics = allocationService.getAllocationStatistics();
        return ApiResponse.ok(statistics);
    }
}

