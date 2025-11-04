package com.ihome.service;

import com.ihome.entity.Notification;
import com.ihome.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 消息通知服务类
 * 处理通知的创建、发送、查询、管理等功能
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 创建并发送通知
     */
    @Transactional
    public Map<String, Object> createNotification(Notification notification) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 设置默认值
            if (notification.getIsRead() == null) {
                notification.setIsRead(false);
            }
            if (notification.getCreateTime() == null) {
                notification.setCreateTime(LocalDateTime.now());
            }
            if (notification.getPriority() == null) {
                notification.setPriority("normal");
            }
            
            // 保存通知
            notificationMapper.insert(notification);
            
            result.put("success", true);
            result.put("message", "通知创建成功");
            result.put("notificationId", notification.getId());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "通知创建失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 批量创建通知
     */
    @Transactional
    public Map<String, Object> createBatchNotifications(List<Notification> notifications) {
        Map<String, Object> result = new HashMap<>();
        List<Integer> successIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (Notification notification : notifications) {
            Map<String, Object> createResult = createNotification(notification);
            if ((Boolean) createResult.get("success")) {
                successIds.add((Integer) createResult.get("notificationId"));
            } else {
                errors.add((String) createResult.get("message"));
            }
        }
        
        result.put("success", successIds.size() > 0);
        result.put("successCount", successIds.size());
        result.put("errorCount", errors.size());
        result.put("successIds", successIds);
        result.put("errors", errors);
        
        return result;
    }

    /**
     * 发送系统通知给所有用户
     * 改为只发送一条通知，接收者ID为"ALL"
     */
    @Transactional
    public Map<String, Object> sendSystemNotification(String title, String content, String type, String priority) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 只创建一条通知，接收者ID为"ALL"，表示全员通知
            Notification notification = new Notification();
            notification.setReceiverId("ALL");
            notification.setReceiverType("all");  // 接收者类型设为"all"表示全员
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(type);
            notification.setPriority(priority);
            notification.setSenderId("system");
            notification.setSenderType("system");
            notification.setIsRead(false);
            notification.setCreateTime(LocalDateTime.now());
            
            // 创建通知
            Map<String, Object> createResult = createNotification(notification);
            
            result.put("success", true);
            result.put("message", "系统通知发送成功");
            result.put("notificationId", notification.getId());
            result.put("details", createResult);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "系统通知发送失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 发送维修通知
     */
    @Transactional
    public Map<String, Object> sendRepairNotification(String repairId, String studentId, String title, String content, String priority) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Notification notification = new Notification();
            notification.setReceiverId(studentId);
            notification.setReceiverType("student");
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType("repair");
            notification.setPriority(priority);
            notification.setRelatedId(repairId);
            notification.setRelatedType("repair");
            notification.setSenderId("system");
            notification.setSenderType("system");
            
            return createNotification(notification);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "维修通知发送失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 发送调换申请通知
     */
    @Transactional
    public Map<String, Object> sendSwitchNotification(String switchId, String receiverId, String receiverType, String title, String content, String priority) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Notification notification = new Notification();
            notification.setReceiverId(receiverId);
            notification.setReceiverType(receiverType);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType("switch");
            notification.setPriority(priority);
            notification.setRelatedId(switchId);
            notification.setRelatedType("switch");
            notification.setSenderId("system");
            notification.setSenderType("system");
            
            return createNotification(notification);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "调换通知发送失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 发送缴费通知
     */
    @Transactional
    public Map<String, Object> sendPaymentNotification(String paymentId, String studentId, String title, String content, String priority) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Notification notification = new Notification();
            notification.setReceiverId(studentId);
            notification.setReceiverType("student");
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType("payment");
            notification.setPriority(priority);
            notification.setRelatedId(paymentId);
            notification.setRelatedType("payment");
            notification.setSenderId("system");
            notification.setSenderType("system");
            
            return createNotification(notification);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "缴费通知发送失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取用户通知列表
     */
    public List<Notification> getUserNotifications(String receiverId, String receiverType) {
        return notificationMapper.selectByReceiver(receiverId, receiverType);
    }

    /**
     * 获取用户未读通知
     */
    public List<Notification> getUnreadNotifications(String receiverId, String receiverType) {
        return notificationMapper.selectUnreadByReceiver(receiverId, receiverType);
    }

    /**
     * 获取用户未读通知数量
     */
    public Integer getUnreadCount(String receiverId, String receiverType) {
        return notificationMapper.countUnreadByReceiver(receiverId, receiverType);
    }

    /**
     * 根据类型获取通知
     */
    public List<Notification> getNotificationsByType(String receiverId, String receiverType, String type) {
        // 如果是系统公告，使用特殊查询方法
        if ("ALL".equals(receiverId) && "system".equals(receiverType)) {
            return notificationMapper.selectSystemNotifications(receiverType, type);
        }
        return notificationMapper.selectByType(receiverId, receiverType, type);
    }
    
    /**
     * 获取系统公告
     */
    public List<Notification> getSystemNotifications(String type) {
        return notificationMapper.selectSystemNotifications("system", type);
    }

    /**
     * 根据优先级获取通知
     */
    public List<Notification> getNotificationsByPriority(String receiverId, String receiverType, String priority) {
        return notificationMapper.selectByPriority(receiverId, receiverType, priority);
    }

    /**
     * 获取有效通知（未过期）
     */
    public List<Notification> getValidNotifications(String receiverId, String receiverType) {
        return notificationMapper.selectValidByReceiver(receiverId, receiverType, LocalDateTime.now());
    }

    /**
     * 标记通知为已读
     */
    @Transactional
    public Map<String, Object> markAsRead(Integer notificationId, String receiverId, String receiverType) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                result.put("success", false);
                result.put("message", "通知不存在");
                return result;
            }
            
            // 对于ALL通知，允许任何用户标记为已读
            // 对于普通通知，检查权限
            boolean isAllNotification = "ALL".equals(notification.getReceiverId()) || "all".equals(notification.getReceiverId());
            if (!isAllNotification) {
                if (!notification.getReceiverId().equals(receiverId) || !notification.getReceiverType().equals(receiverType)) {
                    result.put("success", false);
                    result.put("message", "无权限操作此通知");
                    return result;
                }
            }
            
            // 注意：对于ALL通知，直接标记为已读会影响所有用户
            // 如果需要在每个用户层面独立管理ALL通知的已读状态，需要创建独立的已读记录表
            // 当前实现为简化版本，ALL通知标记已读后，所有用户都会看到已读状态
            notification.setIsRead(true);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
            
            result.put("success", true);
            result.put("message", "通知已标记为已读");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "标记失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 批量标记为已读
     */
    @Transactional
    public Map<String, Object> markMultipleAsRead(List<Integer> notificationIds, String receiverId, String receiverType) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证权限（ALL通知允许任何用户操作）
            for (Integer id : notificationIds) {
                Notification notification = notificationMapper.selectById(id);
                if (notification != null) {
                    boolean isAllNotification = "ALL".equals(notification.getReceiverId()) || "all".equals(notification.getReceiverId());
                    if (!isAllNotification && 
                        (!notification.getReceiverId().equals(receiverId) || !notification.getReceiverType().equals(receiverType))) {
                        result.put("success", false);
                        result.put("message", "无权限操作某些通知");
                        return result;
                    }
                }
            }
            
            // 批量更新
            String ids = String.join(",", notificationIds.stream().map(String::valueOf).toArray(String[]::new));
            int updated = notificationMapper.markAsReadByIds(ids, LocalDateTime.now());
            
            result.put("success", true);
            result.put("message", "批量标记成功");
            result.put("updatedCount", updated);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量标记失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 标记所有通知为已读
     */
    @Transactional
    public Map<String, Object> markAllAsRead(String receiverId, String receiverType) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int updated = notificationMapper.markAllAsRead(receiverId, receiverType, LocalDateTime.now());
            
            result.put("success", true);
            result.put("message", "所有通知已标记为已读");
            result.put("updatedCount", updated);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "标记失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 删除通知
     */
    @Transactional
    public Map<String, Object> deleteNotification(Integer notificationId, String receiverId, String receiverType) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                result.put("success", false);
                result.put("message", "通知不存在");
                return result;
            }
            
            if (!notification.getReceiverId().equals(receiverId) || !notification.getReceiverType().equals(receiverType)) {
                result.put("success", false);
                result.put("message", "无权限删除此通知");
                return result;
            }
            
            notificationMapper.deleteById(notificationId);
            
            result.put("success", true);
            result.put("message", "通知删除成功");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 清理过期通知
     */
    @Transactional
    public Map<String, Object> cleanExpiredNotifications() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int deleted = notificationMapper.deleteExpiredNotifications(LocalDateTime.now());
            
            result.put("success", true);
            result.put("message", "过期通知清理完成");
            result.put("deletedCount", deleted);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "清理失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 根据相关业务查询通知
     */
    public List<Notification> getNotificationsByRelated(String relatedId, String relatedType) {
        return notificationMapper.selectByRelated(relatedId, relatedType);
    }

    /**
     * 根据ID获取通知详情
     */
    public Notification getNotificationById(Integer id) {
        return notificationMapper.selectById(id);
    }

    /**
     * 按ID更新通知的基础字段
     */
    @Transactional
    public boolean updateNotificationSelective(Notification update) {
        if (update == null || update.getId() == null) return false;
        Notification existing = notificationMapper.selectById(update.getId());
        if (existing == null) return false;
        if (update.getTitle() != null) existing.setTitle(update.getTitle());
        if (update.getContent() != null) existing.setContent(update.getContent());
        if (update.getPriority() != null) existing.setPriority(update.getPriority());
        if (update.getType() != null) existing.setType(update.getType());
        return notificationMapper.updateById(existing) > 0;
    }

    /**
     * 按ID删除通知（管理员）
     */
    @Transactional
    public boolean deleteById(Integer id) {
        if (id == null) return false;
        return notificationMapper.deleteById(id) > 0;
    }
}
