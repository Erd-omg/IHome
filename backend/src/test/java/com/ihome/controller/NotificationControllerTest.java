package com.ihome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihome.entity.Notification;
import com.ihome.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 通知控制器测试
 */
@WebMvcTest(
    controllers = NotificationController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        SqlInitializationAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        MybatisPlusAutoConfiguration.class
    }
)
@ActiveProfiles("test")
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

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
    void testMarkAsRead_Success() throws Exception {
        // 准备测试数据
        Integer notificationId = 1;
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "标记已读成功");

        // 模拟服务调用
        when(notificationService.markAsRead(eq(notificationId), anyString(), anyString()))
                .thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(post("/notifications/{id}/read", notificationId)
                        .param("userId", "2024001")
                        .param("userType", "student")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(notificationService).markAsRead(eq(notificationId), anyString(), anyString());
    }

    @Test
    void testMarkAsRead_NotFound() throws Exception {
        // 准备测试数据
        Integer notificationId = 999;
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", false);
        serviceResult.put("message", "通知不存在");

        // 模拟服务调用
        when(notificationService.markAsRead(eq(notificationId), anyString(), anyString()))
                .thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(post("/notifications/{id}/read", notificationId)
                        .param("userId", "2024001")
                        .param("userType", "student")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("通知不存在"));

        // 验证调用
        verify(notificationService).markAsRead(eq(notificationId), anyString(), anyString());
    }

    @Test
    void testBatchMarkAsRead_Success() throws Exception {
        // 准备测试数据
        List<Integer> notificationIds = Arrays.asList(1, 2, 3);
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "批量标记成功");
        serviceResult.put("count", 3);

        // 模拟服务调用
        when(notificationService.markMultipleAsRead(anyList(), anyString(), anyString()))
                .thenReturn(serviceResult);

        // 准备请求体
        Map<String, Object> request = new HashMap<>();
        request.put("notificationIds", notificationIds);

        // 执行测试
        mockMvc.perform(put("/notifications/batch-read")
                        .param("userId", "2024001")
                        .param("userType", "student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationIds))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.count").value(3));

        // 验证调用
        verify(notificationService).markMultipleAsRead(anyList(), anyString(), anyString());
    }

    @Test
    void testMarkAllAsRead_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "所有通知已标记为已读");
        serviceResult.put("updatedCount", 5);

        // 模拟服务调用
        when(notificationService.markAllAsRead(anyString(), anyString()))
                .thenReturn(serviceResult);

        // 执行测试（注意实际路径是/mark-all-read）
        mockMvc.perform(put("/notifications/mark-all-read")
                        .param("userId", "2024001")
                        .param("userType", "student")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.updatedCount").value(5));

        // 验证调用
        verify(notificationService).markAllAsRead(anyString(), anyString());
    }

    @Test
    void testDeleteNotification_Success() throws Exception {
        // 准备测试数据
        Integer notificationId = 1;
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "删除成功");

        // 模拟服务调用
        when(notificationService.deleteNotification(eq(notificationId), anyString(), anyString()))
                .thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(delete("/notifications/{id}", notificationId)
                        .param("userId", "2024001")
                        .param("userType", "student")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(notificationService).deleteNotification(eq(notificationId), anyString(), anyString());
    }

    @Test
    void testGetUnreadCount_Success() throws Exception {
        // 准备测试数据
        Integer unreadCount = 3;

        // 模拟服务调用
        when(notificationService.getUnreadCount(anyString(), anyString()))
                .thenReturn(unreadCount);

        // 执行测试
        mockMvc.perform(get("/notifications/unread-count")
                        .param("userId", "2024001")
                        .param("userType", "student")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(3));

        // 验证调用
        verify(notificationService).getUnreadCount(anyString(), anyString());
    }
}

