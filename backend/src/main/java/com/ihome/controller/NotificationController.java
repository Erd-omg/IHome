package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.entity.Notification;
import com.ihome.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知状态管理控制器
 * 处理通知的读取状态管理
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 标记通知为已读
     */
    @PostMapping("/{id}/read")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> markAsRead(@PathVariable Integer id,
                                                       @RequestParam(required = false) String userId,
                                                       @RequestParam(required = false) String userType) {
        try {
            // 从JWT获取当前用户信息
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String currentUserId;
            if (userId == null || userId.isEmpty()) {
                if (authentication == null || authentication.getName() == null) {
                    return ApiResponse.error("未获取到用户ID");
                }
                currentUserId = authentication.getName();
            } else {
                currentUserId = userId;
            }
            if (userType == null || userType.isEmpty()) {
                userType = getCurrentUserType();
            }
            
            Map<String, Object> result = notificationService.markAsRead(id, currentUserId, userType);
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("标记已读失败: " + e.getMessage());
        }
    }

    /**
     * 批量标记为已读
     */
    @PutMapping("/batch-read")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> markMultipleAsRead(@RequestBody List<Integer> notificationIds,
                                                              @RequestParam(required = false) String userId,
                                                              @RequestParam(required = false) String userType) {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String currentUserId;
            if (userId == null || userId.isEmpty()) {
                if (authentication == null || authentication.getName() == null) {
                    return ApiResponse.error("未获取到用户ID");
                }
                currentUserId = authentication.getName();
            } else {
                currentUserId = userId;
            }
            if (userType == null || userType.isEmpty()) {
                userType = getCurrentUserType();
            }
            
            Map<String, Object> result = notificationService.markMultipleAsRead(notificationIds, currentUserId, userType);
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("批量标记已读失败: " + e.getMessage());
        }
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/mark-all-read")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> markAllAsRead(@RequestParam(required = false) String userId,
                                                          @RequestParam(required = false) String userType) {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String currentUserId;
            if (userId == null || userId.isEmpty()) {
                if (authentication == null || authentication.getName() == null) {
                    return ApiResponse.error("未获取到用户ID");
                }
                currentUserId = authentication.getName();
            } else {
                currentUserId = userId;
            }
            if (userType == null || userType.isEmpty()) {
                userType = getCurrentUserType();
            }
            
            Map<String, Object> result = notificationService.markAllAsRead(currentUserId, userType);
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("标记所有已读失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<Integer> getUnreadCount(@RequestParam(required = false) String userId,
                                                @RequestParam(required = false) String userType) {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String currentUserId;
            if (userId == null || userId.isEmpty()) {
                if (authentication == null || authentication.getName() == null) {
                    return ApiResponse.error("未获取到用户ID");
                }
                currentUserId = authentication.getName();
            } else {
                currentUserId = userId;
            }
            if (userType == null || userType.isEmpty()) {
                userType = getCurrentUserType();
            }
            
            Integer count = notificationService.getUnreadCount(currentUserId, userType);
            return ApiResponse.ok(count);
        } catch (Exception e) {
            return ApiResponse.error("获取未读数量失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户通知列表
     */
    @GetMapping("/my-notifications")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<List<Notification>> getMyNotifications() {
        try {
            String currentUserId = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
            String userType = getCurrentUserType();
            
            List<Notification> notifications = notificationService.getUserNotifications(currentUserId, userType);
            return ApiResponse.ok(notifications);
        } catch (Exception e) {
            return ApiResponse.error("获取通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读通知列表
     */
    @GetMapping("/unread")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<List<Notification>> getUnreadNotifications() {
        try {
            String currentUserId = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
            String userType = getCurrentUserType();
            
            List<Notification> notifications = notificationService.getUnreadNotifications(currentUserId, userType);
            return ApiResponse.ok(notifications);
        } catch (Exception e) {
            return ApiResponse.error("获取未读通知失败: " + e.getMessage());
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> deleteNotification(@PathVariable Integer id,
                                                               @RequestParam(required = false) String userId,
                                                               @RequestParam(required = false) String userType) {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String currentUserId;
            if (userId == null || userId.isEmpty()) {
                if (authentication == null || authentication.getName() == null) {
                    return ApiResponse.error("未获取到用户ID");
                }
                currentUserId = authentication.getName();
            } else {
                currentUserId = userId;
            }
            if (userType == null || userType.isEmpty()) {
                userType = getCurrentUserType();
            }
            
            Map<String, Object> result = notificationService.deleteNotification(id, currentUserId, userType);
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("删除通知失败: " + e.getMessage());
        }
    }

    /**
     * 发送系统通知
     */
    @PostMapping("/send-system")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> sendSystemNotification(@RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String content = request.get("content");
            String type = request.getOrDefault("type", "system");
            String priority = request.getOrDefault("priority", "normal");
            
            Map<String, Object> result = notificationService.sendSystemNotification(title, content, type, priority);
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("发送系统通知失败: " + e.getMessage());
        }
    }

    /**
     * 创建通知
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> createNotification(@RequestBody Notification notification) {
        try {
            Map<String, Object> result = notificationService.createNotification(notification);
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("创建通知失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期通知
     */
    @PostMapping("/clean-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> cleanExpiredNotifications() {
        try {
            Map<String, Object> result = notificationService.cleanExpiredNotifications();
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("清理过期通知失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户类型
     */
    private String getCurrentUserType() {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null || authentication.getAuthorities().isEmpty()) {
            return "student";  // 默认返回student，用于测试环境
        }
        return authentication.getAuthorities().iterator().next().getAuthority()
            .replace("ROLE_", "").toLowerCase();
    }
}