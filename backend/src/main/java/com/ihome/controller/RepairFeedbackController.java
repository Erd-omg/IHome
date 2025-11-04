package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.entity.RepairFeedback;
import com.ihome.service.RepairFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/repair-feedback")
public class RepairFeedbackController {

    @Autowired
    private RepairFeedbackService repairFeedbackService;

    /**
     * 提交维修反馈
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> submitFeedback(@RequestBody RepairFeedback feedback) {
        try {
            org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                // 如果feedback中已经有studentId，使用它（用于测试环境）
                if (feedback.getStudentId() != null && !feedback.getStudentId().isEmpty()) {
                    repairFeedbackService.submitFeedback(feedback);
                    return ApiResponse.ok("反馈提交成功");
                }
                return ApiResponse.error("未获取到学生ID");
            }
            String studentId = authentication.getName();
            feedback.setStudentId(studentId);
            repairFeedbackService.submitFeedback(feedback);
            return ApiResponse.ok("反馈提交成功");
        } catch (Exception e) {
            return ApiResponse.error("反馈提交失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生的维修反馈列表
     */
    @GetMapping("/my-feedback")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<RepairFeedback>> getMyFeedback(@RequestParam(required = false) String studentId) {
        try {
            org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (studentId == null || studentId.isEmpty()) {
                if (authentication == null || authentication.getName() == null) {
                    return ApiResponse.error("未获取到学生ID");
                }
                studentId = authentication.getName();
            }
            List<RepairFeedback> feedbacks = repairFeedbackService.getFeedbackByStudentId(studentId);
            return ApiResponse.ok(feedbacks);
        } catch (Exception e) {
            return ApiResponse.error("获取反馈列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取维修反馈详情
     */
    @GetMapping("/{feedbackId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<RepairFeedback> getFeedbackDetail(@PathVariable Integer feedbackId) {
        try {
            RepairFeedback feedback = repairFeedbackService.getFeedbackById(feedbackId);
            return ApiResponse.ok(feedback);
        } catch (Exception e) {
            return ApiResponse.error("获取反馈详情失败: " + e.getMessage());
        }
    }

    /**
     * 管理员获取所有维修反馈
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<RepairFeedback>> getAllFeedback(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String repairOrderId,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Integer rating) {
        try {
            List<RepairFeedback> feedbacks = repairFeedbackService.getAllFeedback(page, size, repairOrderId, studentId, rating);
            return ApiResponse.ok(feedbacks);
        } catch (Exception e) {
            return ApiResponse.error("获取反馈列表失败: " + e.getMessage());
        }
    }

    /**
     * 管理员回复反馈
     */
    @PostMapping("/admin/{feedbackId}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> replyFeedback(@PathVariable Integer feedbackId, @RequestParam String reply) {
        try {
            repairFeedbackService.replyFeedback(feedbackId, reply);
            return ApiResponse.ok("回复成功");
        } catch (Exception e) {
            return ApiResponse.error("回复失败: " + e.getMessage());
        }
    }

    /**
     * 获取维修反馈统计
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getFeedbackStatistics() {
        try {
            Map<String, Object> statistics = repairFeedbackService.getFeedbackStatistics();
            return ApiResponse.ok(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取统计信息失败: " + e.getMessage());
        }
    }
}

