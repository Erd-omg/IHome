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
 * 公告通知控制器
 * 处理公告相关接口，兼容前端的notices端点
 */
@RestController
@RequestMapping("/notices")
public class NoticeController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取公告列表（分页）
     */
    @GetMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getNotices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String receiverId,
            @RequestParam(required = false) String receiverType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            // 如果没有指定接收者，返回系统公告
            if (receiverId == null || receiverType == null) {
                // 查询receiver_id='ALL'且receiver_type='system'的系统公告
                List<Notification> notices = notificationService.getNotificationsByType("ALL", "system", "system");
                
                // 应用搜索过滤
                if (keyword != null && !keyword.trim().isEmpty()) {
                    notices = notices.stream()
                            .filter(notice -> notice.getTitle().contains(keyword) || 
                                           notice.getContent().contains(keyword))
                            .collect(java.util.stream.Collectors.toList());
                }
                
                if (startDate != null && !startDate.trim().isEmpty()) {
                    notices = notices.stream()
                            .filter(notice -> notice.getCreateTime().toString().compareTo(startDate) >= 0)
                            .collect(java.util.stream.Collectors.toList());
                }
                
                if (endDate != null && !endDate.trim().isEmpty()) {
                    notices = notices.stream()
                            .filter(notice -> notice.getCreateTime().toString().compareTo(endDate) <= 0)
                            .collect(java.util.stream.Collectors.toList());
                }
                
                // 分页处理
                int start = (page - 1) * size;
                int end = Math.min(start + size, notices.size());
                List<Notification> pagedNotices = notices.subList(start, end);
                
                Map<String, Object> result = Map.of(
                    "content", pagedNotices,
                    "totalElements", notices.size(),
                    "totalPages", (int) Math.ceil((double) notices.size() / size),
                    "size", size,
                    "number", page - 1,
                    "first", page == 1,
                    "last", page >= Math.ceil((double) notices.size() / size)
                );
                return ApiResponse.ok(result);
            } else {
                // 获取用户的通知
                List<Notification> notices = notificationService.getUserNotifications(receiverId, receiverType);
                
                // 应用搜索过滤
                if (keyword != null && !keyword.trim().isEmpty()) {
                    notices = notices.stream()
                            .filter(notice -> notice.getTitle().contains(keyword) || 
                                           notice.getContent().contains(keyword))
                            .collect(java.util.stream.Collectors.toList());
                }
                
                if (startDate != null && !startDate.trim().isEmpty()) {
                    notices = notices.stream()
                            .filter(notice -> notice.getCreateTime().toString().compareTo(startDate) >= 0)
                            .collect(java.util.stream.Collectors.toList());
                }
                
                if (endDate != null && !endDate.trim().isEmpty()) {
                    notices = notices.stream()
                            .filter(notice -> notice.getCreateTime().toString().compareTo(endDate) <= 0)
                            .collect(java.util.stream.Collectors.toList());
                }
                
                // 分页处理
                int start = (page - 1) * size;
                int end = Math.min(start + size, notices.size());
                List<Notification> pagedNotices = notices.subList(start, end);
                
                Map<String, Object> result = Map.of(
                    "content", pagedNotices,
                    "totalElements", notices.size(),
                    "totalPages", (int) Math.ceil((double) notices.size() / size),
                    "size", size,
                    "number", page - 1,
                    "first", page == 1,
                    "last", page >= Math.ceil((double) notices.size() / size)
                );
                return ApiResponse.ok(result);
            }
        } catch (Exception e) {
            return ApiResponse.error("获取公告列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<Notification> getNoticeById(@PathVariable Integer id) {
        try {
            Notification notice = notificationService.getNotificationById(id);
            if (notice != null) {
                return ApiResponse.ok(notice);
            } else {
                return ApiResponse.error("公告不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("获取公告详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建公告（管理员功能）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> createNotice(@RequestBody Notification notice) {
        try {
            notice.setType("system");
            notice.setPriority("normal");
            Map<String, Object> result = notificationService.createNotification(notice);
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("创建公告失败: " + e.getMessage());
        }
    }

    /**
     * 更新公告（管理员功能）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> updateNotice(@PathVariable Integer id, @RequestBody Notification notice) {
        try {
            Notification existing = notificationService.getNotificationById(id);
            if (existing == null) return ApiResponse.error("公告不存在");
            notice.setId(id);
            boolean ok = notificationService.updateNotificationSelective(notice);
            if (ok) {
                return ApiResponse.ok(Map.of("message", "公告更新成功"));
            }
            return ApiResponse.error("公告更新失败");
        } catch (Exception e) {
            return ApiResponse.error("更新公告失败: " + e.getMessage());
        }
    }

    /**
     * 删除公告（管理员功能）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> deleteNotice(@PathVariable Integer id) {
        try {
            Notification existing = notificationService.getNotificationById(id);
            if (existing == null) return ApiResponse.error("公告不存在");
            boolean ok = notificationService.deleteById(id);
            if (ok) return ApiResponse.ok(Map.of("message", "公告删除成功"));
            return ApiResponse.error("公告删除失败");
        } catch (Exception e) {
            return ApiResponse.error("删除公告失败: " + e.getMessage());
        }
    }
}

