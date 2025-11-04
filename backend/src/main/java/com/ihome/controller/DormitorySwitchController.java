package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.entity.DormitorySwitch;
import com.ihome.service.DormitorySwitchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 宿舍调换控制器
 * 处理宿舍调换申请和审核相关接口
 */
@RestController
@RequestMapping("/switches")
public class DormitorySwitchController {

    @Autowired
    private DormitorySwitchService switchService;

    /**
     * 学生提交调换申请
     */
    @PostMapping("/apply")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Map<String, Object>> submitSwitchRequest(@RequestBody DormitorySwitch switchRequest) {
        try {
            Map<String, Object> result = switchService.submitSwitchRequest(switchRequest);
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("提交申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生的调换申请列表
     */
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<DormitorySwitch>> getMySwitchRequests(@RequestParam String studentId) {
        try {
            List<DormitorySwitch> requests = switchService.getStudentSwitchRequests(studentId);
            return ApiResponse.ok(requests);
        } catch (Exception e) {
            return ApiResponse.error("获取申请列表失败: " + e.getMessage());
        }
    }

    /**
     * 学生取消调换申请
     */
    @PostMapping("/{switchId}/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Map<String, Object>> cancelSwitchRequest(
            @PathVariable Integer switchId,
            @RequestParam String studentId) {
        try {
            Map<String, Object> result = switchService.cancelSwitchRequest(switchId, studentId);
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("取消申请失败: " + e.getMessage());
        }
    }

    /**
     * 管理员获取待审核的调换申请列表
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<DormitorySwitch>> getPendingSwitchRequests() {
        try {
            List<DormitorySwitch> requests = switchService.getPendingSwitchRequests();
            return ApiResponse.ok(requests);
        } catch (Exception e) {
            return ApiResponse.error("获取待审核申请失败: " + e.getMessage());
        }
    }

    /**
     * 管理员获取所有调换申请
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<DormitorySwitch>> getAllSwitchRequests() {
        try {
            List<DormitorySwitch> requests = switchService.getAllSwitchRequests();
            return ApiResponse.ok(requests);
        } catch (Exception e) {
            return ApiResponse.error("获取申请列表失败: " + e.getMessage());
        }
    }

    /**
     * 管理员审核调换申请
     */
    @PostMapping("/{switchId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> reviewSwitchRequest(
            @PathVariable Integer switchId,
            @RequestBody ReviewRequest request) {
        try {
            Map<String, Object> result = switchService.reviewSwitchRequest(
                    switchId, 
                    request.getStatus(), 
                    request.getReviewerId(), 
                    request.getReviewComment()
            );
            
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("审核失败: " + e.getMessage());
        }
    }

    /**
     * 获取调换申请详情
     */
    @GetMapping("/{switchId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<DormitorySwitch> getSwitchRequestDetails(@PathVariable Integer switchId) {
        try {
            // 这里可以添加权限检查，确保学生只能查看自己的申请
            // 管理员可以查看所有申请
            return ApiResponse.ok(null); // 暂时返回null，需要实现具体逻辑
        } catch (Exception e) {
            return ApiResponse.error("获取申请详情失败: " + e.getMessage());
        }
    }

    /**
     * 审核请求DTO
     */
    public static class ReviewRequest {
        private String status;
        private String reviewerId;
        private String reviewComment;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getReviewerId() {
            return reviewerId;
        }

        public void setReviewerId(String reviewerId) {
            this.reviewerId = reviewerId;
        }

        public String getReviewComment() {
            return reviewComment;
        }

        public void setReviewComment(String reviewComment) {
            this.reviewComment = reviewComment;
        }
    }
}

