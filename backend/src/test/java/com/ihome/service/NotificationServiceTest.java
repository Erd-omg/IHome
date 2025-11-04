package com.ihome.service;

import com.ihome.entity.Notification;
import com.ihome.mapper.NotificationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 通知服务测试
 */
@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 创建测试通知
        testNotification = new Notification();
        testNotification.setId(1);
        testNotification.setReceiverId("2024001");
        testNotification.setReceiverType("student");
        testNotification.setTitle("测试通知");
        testNotification.setContent("这是一条测试通知");
        testNotification.setType("system");
        testNotification.setPriority("normal");
        testNotification.setIsRead(false);
        testNotification.setCreateTime(LocalDateTime.now());
    }

    @Test
    void testCreateNotification_Success() {
        // 准备测试数据
        Notification notification = new Notification();
        notification.setReceiverId("2024001");
        notification.setReceiverType("student");
        notification.setTitle("新通知");
        notification.setContent("通知内容");

        // 模拟数据库操作
        when(notificationMapper.insert(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(1);
            return 1;
        });

        // 执行测试
        Map<String, Object> result = notificationService.createNotification(notification);

        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("通知创建成功", result.get("message"));
        assertNotNull(result.get("notificationId"));

        // 验证调用
        verify(notificationMapper).insert(any(Notification.class));
    }

    @Test
    void testCreateNotification_Exception() {
        // 准备测试数据
        Notification notification = new Notification();
        notification.setReceiverId("2024001");
        notification.setReceiverType("student");

        // 模拟数据库操作抛出异常
        when(notificationMapper.insert(any(Notification.class))).thenThrow(new RuntimeException("数据库错误"));

        // 执行测试
        Map<String, Object> result = notificationService.createNotification(notification);

        // 验证结果
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(((String) result.get("message")).contains("通知创建失败"));

        // 验证调用
        verify(notificationMapper).insert(any(Notification.class));
    }

    @Test
    void testMarkAsRead_Success() {
        // 准备测试数据
        Integer notificationId = 1;
        String userId = "2024001";
        String userType = "student";

        // 模拟数据库查询和更新
        when(notificationMapper.selectById(notificationId)).thenReturn(testNotification);
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        // 执行测试
        Map<String, Object> result = notificationService.markAsRead(notificationId, userId, userType);

        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("通知已标记为已读", result.get("message"));

        // 验证调用
        verify(notificationMapper).selectById(notificationId);
        verify(notificationMapper).updateById(any(Notification.class));
    }

    @Test
    void testMarkAsRead_NotFound() {
        // 准备测试数据
        Integer notificationId = 999;
        String userId = "2024001";
        String userType = "student";

        // 模拟通知不存在
        when(notificationMapper.selectById(notificationId)).thenReturn(null);

        // 执行测试
        Map<String, Object> result = notificationService.markAsRead(notificationId, userId, userType);

        // 验证结果
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("通知不存在", result.get("message"));

        // 验证调用
        verify(notificationMapper).selectById(notificationId);
        verify(notificationMapper, never()).updateById(any(Notification.class));
    }

    @Test
    void testMarkMultipleAsRead_Success() {
        // 准备测试数据
        List<Integer> notificationIds = Arrays.asList(1, 2, 3);
        String userId = "2024001";
        String userType = "student";

        // 模拟数据库查询和更新
        when(notificationMapper.selectById(anyInt())).thenReturn(testNotification);
        when(notificationMapper.markAsReadByIds(anyString(), any())).thenReturn(3);

        // 执行测试
        Map<String, Object> result = notificationService.markMultipleAsRead(notificationIds, userId, userType);

        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals(3, result.get("updatedCount"));

        // 验证调用
        verify(notificationMapper, atLeastOnce()).selectById(anyInt());
        verify(notificationMapper).markAsReadByIds(anyString(), any());
    }

    @Test
    void testGetUnreadCount_Success() {
        // 准备测试数据
        String userId = "2024001";
        String userType = "student";
        Integer expectedCount = 3;

        // 模拟数据库查询
        when(notificationMapper.countUnreadByReceiver(userId, userType)).thenReturn(expectedCount);

        // 执行测试
        Integer result = notificationService.getUnreadCount(userId, userType);

        // 验证结果
        assertNotNull(result);
        assertEquals(expectedCount, result);

        // 验证调用
        verify(notificationMapper).countUnreadByReceiver(userId, userType);
    }

}

