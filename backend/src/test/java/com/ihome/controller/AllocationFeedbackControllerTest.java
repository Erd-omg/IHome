package com.ihome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihome.entity.AllocationFeedback;
import com.ihome.service.AllocationService;
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
 * 分配反馈控制器测试
 */
@WebMvcTest(
    controllers = AllocationFeedbackController.class,
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
public class AllocationFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AllocationService allocationService;

    @Autowired
    private ObjectMapper objectMapper;

    private AllocationFeedback testFeedback;
    private List<AllocationFeedback> testFeedbacks;
    private Map<String, Object> testStatistics;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 创建测试反馈
        testFeedback = new AllocationFeedback();
        testFeedback.setId(1L);
        testFeedback.setStudentId("S001");
        testFeedback.setAllocationId(1L);
        testFeedback.setRoommateSatisfaction(4);
        testFeedback.setEnvironmentSatisfaction(5);
        testFeedback.setOverallSatisfaction(4);
        testFeedback.setFeedbackContent("分配结果很满意");
        testFeedback.setFeedbackTime(LocalDateTime.now());
        testFeedback.setCreatedAt(LocalDateTime.now());

        // 创建测试反馈列表
        testFeedbacks = Arrays.asList(
            createFeedback("S001", 4, 5, 4, "满意"),
            createFeedback("S001", 3, 3, 3, "一般")
        );

        // 创建测试统计数据
        testStatistics = new HashMap<>();
        testStatistics.put("totalFeedbacks", 2);
        testStatistics.put("averageRoommateSatisfaction", 3.5);
        testStatistics.put("averageEnvironmentSatisfaction", 4.0);
        testStatistics.put("averageOverallSatisfaction", 3.5);
        testStatistics.put("satisfactionDistribution", Map.of("3分", 1L, "4分", 1L));
        testStatistics.put("currentWeights", Map.of(
            "QUESTIONNAIRE", 0.5,
            "TAG", 0.4,
            "MAJOR", 0.3
        ));
    }

    private AllocationFeedback createFeedback(String studentId, int roommate, int environment, int overall, String comments) {
        AllocationFeedback feedback = new AllocationFeedback();
        feedback.setStudentId(studentId);
        feedback.setAllocationId(1L);
        feedback.setRoommateSatisfaction(roommate);
        feedback.setEnvironmentSatisfaction(environment);
        feedback.setOverallSatisfaction(overall);
        feedback.setFeedbackContent(comments);
        feedback.setFeedbackTime(LocalDateTime.now());
        feedback.setCreatedAt(LocalDateTime.now());
        return feedback;
    }

    @Test
    void testSubmitFeedback_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "反馈提交成功");
        serviceResult.put("feedbackId", 1);

        // 模拟服务调用
        when(allocationService.submitAllocationFeedback(any(AllocationFeedback.class)))
                .thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(post("/allocation-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFeedback)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("反馈提交成功"))
                .andExpect(jsonPath("$.data.feedbackId").value(1));

        // 验证服务调用
        verify(allocationService).submitAllocationFeedback(any(AllocationFeedback.class));
    }

    @Test
    void testSubmitFeedback_Failure() throws Exception {
        // 准备测试数据
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", false);
        serviceResult.put("message", "反馈提交失败");

        // 模拟服务调用
        when(allocationService.submitAllocationFeedback(any(AllocationFeedback.class)))
                .thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(post("/allocation-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFeedback)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("反馈提交失败"));

        // 验证服务调用
        verify(allocationService).submitAllocationFeedback(any(AllocationFeedback.class));
    }

    @Test
    void testGetStudentFeedback_Success() throws Exception {
        // 准备测试数据
        String studentId = "S001";

        // 模拟服务调用
        when(allocationService.getStudentFeedback(studentId))
                .thenReturn(testFeedbacks);

        // 执行测试
        mockMvc.perform(get("/allocation-feedback/student/{studentId}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].studentId").value("S001"))
                .andExpect(jsonPath("$.data[0].roommateSatisfaction").value(4))
                .andExpect(jsonPath("$.data[0].environmentSatisfaction").value(5))
                .andExpect(jsonPath("$.data[0].overallSatisfaction").value(4));

        // 验证服务调用
        verify(allocationService).getStudentFeedback(studentId);
    }

    @Test
    void testGetAllocationStatistics_Success() throws Exception {
        // 模拟服务调用
        when(allocationService.getAllocationStatistics())
                .thenReturn(testStatistics);

        // 执行测试
        mockMvc.perform(get("/allocation-feedback/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalFeedbacks").value(2))
                .andExpect(jsonPath("$.data.averageRoommateSatisfaction").value(3.5))
                .andExpect(jsonPath("$.data.averageEnvironmentSatisfaction").value(4.0))
                .andExpect(jsonPath("$.data.averageOverallSatisfaction").value(3.5))
                .andExpect(jsonPath("$.data.satisfactionDistribution").isMap())
                .andExpect(jsonPath("$.data.currentWeights").isMap());

        // 验证服务调用
        verify(allocationService).getAllocationStatistics();
    }

    @Test
    void testSubmitFeedback_InvalidData() throws Exception {
        // 准备无效的测试数据
        AllocationFeedback invalidFeedback = new AllocationFeedback();
        invalidFeedback.setStudentId(""); // 空的学生ID
        invalidFeedback.setRoommateSatisfaction(6); // 超出范围的满意度分数

        // 模拟service返回错误结果（service会捕获异常并返回错误消息）
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", false);
        serviceResult.put("message", "反馈提交失败: null");  // 可能是NullPointerException或其他异常
        when(allocationService.submitAllocationFeedback(any(AllocationFeedback.class))).thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(post("/allocation-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFeedback)))
                .andExpect(status().isOk())  // controller返回200，但success=false
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testSubmitFeedback_EmptyComments() throws Exception {
        // 准备测试数据 - 空评论
        AllocationFeedback feedbackWithEmptyComments = new AllocationFeedback();
        feedbackWithEmptyComments.setStudentId("S001");
        feedbackWithEmptyComments.setAllocationId(1L);
        feedbackWithEmptyComments.setRoommateSatisfaction(4);
        feedbackWithEmptyComments.setEnvironmentSatisfaction(5);
        feedbackWithEmptyComments.setOverallSatisfaction(4);
        feedbackWithEmptyComments.setFeedbackContent(""); // 空评论

        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "反馈提交成功");
        serviceResult.put("feedbackId", 1);

        // 模拟服务调用
        when(allocationService.submitAllocationFeedback(any(AllocationFeedback.class)))
                .thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(post("/allocation-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackWithEmptyComments)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true));

        // 验证服务调用
        verify(allocationService).submitAllocationFeedback(any(AllocationFeedback.class));
    }
}