package com.ihome.controller;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 公告通知控制器测试
 */
@WebMvcTest(
    controllers = NoticeController.class,
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
public class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    private Notification testNotice;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testNotice = new Notification();
        testNotice.setId(1);
        testNotice.setReceiverId("ALL");
        testNotice.setReceiverType("system");
        testNotice.setTitle("系统公告");
        testNotice.setContent("这是一条系统公告");
        testNotice.setType("system");
        testNotice.setCreateTime(LocalDateTime.now());
    }

    @Test
    void testGetNotices_Success() throws Exception {
        // 准备测试数据
        List<Notification> notices = Arrays.asList(testNotice);

        // 模拟服务调用
        when(notificationService.getNotificationsByType("ALL", "system", "system")).thenReturn(notices);

        // 执行测试
        mockMvc.perform(get("/notices")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        verify(notificationService).getNotificationsByType(eq("ALL"), eq("system"), eq("system"));
    }

    @Test
    void testGetNotices_WithKeyword() throws Exception {
        // 准备测试数据
        List<Notification> notices = Arrays.asList(testNotice);

        // 模拟服务调用
        when(notificationService.getNotificationsByType("ALL", "system", "system")).thenReturn(notices);

        // 执行测试（带搜索关键词）
        mockMvc.perform(get("/notices")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "系统")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).getNotificationsByType(eq("ALL"), eq("system"), eq("system"));
    }

    @Test
    void testGetNoticeDetail_Success() throws Exception {
        // 模拟服务调用
        when(notificationService.getNotificationById(1)).thenReturn(testNotice);

        // 执行测试
        mockMvc.perform(get("/notices/1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("系统公告"));

        verify(notificationService).getNotificationById(1);
    }

    @Test
    void testGetNoticeDetail_NotFound() throws Exception {
        // 模拟通知不存在
        when(notificationService.getNotificationById(999)).thenReturn(null);

        // 执行测试
        mockMvc.perform(get("/notices/999")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verify(notificationService).getNotificationById(999);
    }
}

