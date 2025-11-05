package com.ihome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ihome.entity.Notification;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    
    /**
     * 根据接收者ID和类型查询通知
     * 包含用户自己的通知和receiver_id为'ALL'的全员通知
     */
    @Select("SELECT * FROM notifications WHERE (receiver_id = #{receiverId} AND receiver_type = #{receiverType}) OR (UPPER(receiver_id) = 'ALL') ORDER BY create_time DESC")
    List<Notification> selectByReceiver(String receiverId, String receiverType);
    
    /**
     * 查询未读通知
     * 包含用户自己的通知和receiver_id为'ALL'的全员通知
     */
    @Select("SELECT * FROM notifications WHERE ((receiver_id = #{receiverId} AND receiver_type = #{receiverType}) OR (UPPER(receiver_id) = 'ALL')) AND is_read = false ORDER BY create_time DESC")
    List<Notification> selectUnreadByReceiver(String receiverId, String receiverType);
    
    /**
     * 根据通知类型查询
     */
    @Select("SELECT * FROM notifications WHERE receiver_id = #{receiverId} AND receiver_type = #{receiverType} AND type = #{type} ORDER BY create_time DESC")
    List<Notification> selectByType(String receiverId, String receiverType, String type);
    
    /**
     * 查询所有系统公告（不分receiver_id）
     */
    @Select("SELECT * FROM notifications WHERE UPPER(receiver_id) = 'ALL' AND type = #{type} ORDER BY create_time DESC")
    List<Notification> selectSystemNotifications(String type);
    
    /**
     * 根据优先级查询
     */
    @Select("SELECT * FROM notifications WHERE receiver_id = #{receiverId} AND receiver_type = #{receiverType} AND priority = #{priority} ORDER BY create_time DESC")
    List<Notification> selectByPriority(String receiverId, String receiverType, String priority);
    
    /**
     * 查询未过期通知
     */
    @Select("SELECT * FROM notifications WHERE receiver_id = #{receiverId} AND receiver_type = #{receiverType} AND (expire_time IS NULL OR expire_time > #{now}) ORDER BY create_time DESC")
    List<Notification> selectValidByReceiver(String receiverId, String receiverType, LocalDateTime now);
    
    /**
     * 统计未读通知数量
     * 包含用户自己的通知和receiver_id为'ALL'的全员通知
     */
    @Select("SELECT COUNT(*) FROM notifications WHERE ((receiver_id = #{receiverId} AND receiver_type = #{receiverType}) OR (UPPER(receiver_id) = 'ALL')) AND is_read = false")
    Integer countUnreadByReceiver(String receiverId, String receiverType);
    
    /**
     * 批量标记为已读
     */
    @Update("UPDATE notifications SET is_read = true, read_time = #{readTime} WHERE id IN (${ids})")
    int markAsReadByIds(String ids, LocalDateTime readTime);
    
    /**
     * 标记所有通知为已读
     * 包括用户自己的通知和ALL通知
     */
    @Update("UPDATE notifications SET is_read = true, read_time = #{readTime} WHERE (receiver_id = #{receiverId} AND receiver_type = #{receiverType}) OR (UPPER(receiver_id) = 'ALL')")
    int markAllAsRead(String receiverId, String receiverType, LocalDateTime readTime);
    
    /**
     * 删除过期通知
     */
    @Delete("DELETE FROM notifications WHERE expire_time IS NOT NULL AND expire_time < #{now}")
    int deleteExpiredNotifications(LocalDateTime now);
    
    /**
     * 根据相关业务ID查询通知
     */
    @Select("SELECT * FROM notifications WHERE related_id = #{relatedId} AND related_type = #{relatedType} ORDER BY create_time DESC")
    List<Notification> selectByRelated(String relatedId, String relatedType);
}
