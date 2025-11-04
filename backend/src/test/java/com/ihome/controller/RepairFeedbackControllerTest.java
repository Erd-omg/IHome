package com.ihome.controller;

import com.ihome.entity.RepairFeedback;
import com.ihome.service.RepairFeedbackService;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 维修反馈控制器测试
 */
@WebMvcTest(
    controllers = RepairFeedbackController.class,
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
public class RepairFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RepairFeedbackService repairFeedbackService;

    @Autowired
    private ObjectMapper objectMapper;

    private RepairFeedback testFeedback;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testFeedback = new RepairFeedback();
        testFeedback.setId(1);
        testFeedback.setStudentId("2024001");
        testFeedback.setRepairOrderId(1);
        testFeedback.setRating(5);
        testFeedback.setContent("维修服务很好");
        testFeedback.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testSubmitFeedback_Success() throws Exception {
        // 模拟服务调用
        doNothing().when(repairFeedbackService).submitFeedback(any(RepairFeedback.class));

        // 执行测试 - testFeedback已经设置了studentId，controller会使用它
        mockMvc.perform(post("/repair-feedback/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFeedback))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(repairFeedbackService).submitFeedback(any(RepairFeedback.class));
    }

    @Test
    void testGetMyFeedback_Success() throws Exception {
        // 准备测试数据
        List<RepairFeedback> feedbacks = Arrays.asList(testFeedback);

        // 模拟服务调用
        when(repairFeedbackService.getFeedbackByStudentId("2024001")).thenReturn(feedbacks);

        // 执行测试 - 传递studentId参数，因为SecurityContext为空
        mockMvc.perform(get("/repair-feedback/my-feedback")
                        .param("studentId", "2024001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(repairFeedbackService).getFeedbackByStudentId("2024001");
    }

    @Test
    void testGetFeedbackDetail_Success() throws Exception {
        // 模拟服务调用
        when(repairFeedbackService.getFeedbackById(1)).thenReturn(testFeedback);

        // 执行测试
        mockMvc.perform(get("/repair-feedback/1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(repairFeedbackService).getFeedbackById(1);
    }

    @Test
    void testGetAllFeedback_Success() throws Exception {
        // 准备测试数据
        List<RepairFeedback> feedbacks = Arrays.asList(testFeedback);

        // 模拟服务调用
        when(repairFeedbackService.getAllFeedback(1, 10, null, null, null)).thenReturn(feedbacks);

        // 执行测试
        mockMvc.perform(get("/repair-feedback/admin/all")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(repairFeedbackService).getAllFeedback(1, 10, null, null, null);
    }

    @Test
    void testReplyFeedback_Success() throws Exception {
        // 模拟服务调用
        doNothing().when(repairFeedbackService).replyFeedback(1, "已收到反馈，感谢");

        // 执行测试
        mockMvc.perform(post("/repair-feedback/admin/1/reply")
                        .param("reply", "已收到反馈，感谢")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(repairFeedbackService).replyFeedback(1, "已收到反馈，感谢");
    }
}

