package com.ihome.controller;

import com.ihome.entity.DormitorySwitch;
import com.ihome.service.DormitorySwitchService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 宿舍调换控制器测试
 */
@WebMvcTest(
    controllers = DormitorySwitchController.class,
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
public class DormitorySwitchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DormitorySwitchService switchService;

    @Autowired
    private ObjectMapper objectMapper;

    private DormitorySwitch testSwitch;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testSwitch = new DormitorySwitch();
        testSwitch.setId(1);
        testSwitch.setApplicantId("2024001");
        testSwitch.setTargetBedId("B002");
        testSwitch.setReason("希望调换到更好的宿舍");
        testSwitch.setStatus("待审核");
        testSwitch.setApplyTime(LocalDateTime.now());
    }

    @Test
    void testSubmitSwitchRequest_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "申请提交成功");
        serviceResult.put("switchId", 1);

        // 模拟服务调用
        when(switchService.submitSwitchRequest(any(DormitorySwitch.class))).thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(post("/switches/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSwitch))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(switchService).submitSwitchRequest(any(DormitorySwitch.class));
    }

    @Test
    void testGetMySwitchRequests_Success() throws Exception {
        // 准备测试数据
        List<DormitorySwitch> requests = Arrays.asList(testSwitch);

        // 模拟服务调用
        when(switchService.getStudentSwitchRequests("2024001")).thenReturn(requests);

        // 执行测试
        mockMvc.perform(get("/switches/my-requests")
                        .param("studentId", "2024001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(switchService).getStudentSwitchRequests("2024001");
    }

    @Test
    void testCancelSwitchRequest_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "申请已取消");

        // 模拟服务调用
        when(switchService.cancelSwitchRequest(1, "2024001")).thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(post("/switches/1/cancel")
                        .param("studentId", "2024001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(switchService).cancelSwitchRequest(1, "2024001");
    }

    @Test
    void testGetPendingSwitchRequests_Success() throws Exception {
        // 准备测试数据
        List<DormitorySwitch> requests = Arrays.asList(testSwitch);

        // 模拟服务调用
        when(switchService.getPendingSwitchRequests()).thenReturn(requests);

        // 执行测试
        mockMvc.perform(get("/switches/pending")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(switchService).getPendingSwitchRequests();
    }

    @Test
    void testReviewSwitchRequest_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "审核成功");

        // 准备请求体
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "approved");
        requestBody.put("reviewerId", "admin001");
        requestBody.put("reviewComment", "同意调换");

        // 模拟服务调用
        when(switchService.reviewSwitchRequest(eq(1), eq("approved"), eq("admin001"), eq("同意调换"))).thenReturn(serviceResult);

        // 执行测试 - 使用JSON body而不是param
        mockMvc.perform(post("/switches/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(switchService).reviewSwitchRequest(eq(1), eq("approved"), eq("admin001"), eq("同意调换"));
    }
}

