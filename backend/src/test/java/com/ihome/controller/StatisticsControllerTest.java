package com.ihome.controller;

import com.ihome.service.StatisticsService;
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
import com.ihome.common.JwtUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 统计控制器测试
 */
@WebMvcTest(
    controllers = StatisticsController.class,
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
public class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsService statisticsService;
    
    @MockBean
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        // 测试前准备
    }

    @Test
    void testGetDashboardStatistics_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalStudents", 100);
        statistics.put("totalDormitories", 50);

        // 模拟服务调用
        when(statisticsService.getDashboardStatistics()).thenReturn(statistics);

        // 执行测试
        mockMvc.perform(get("/statistics/dashboard")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalStudents").value(100));

        verify(statisticsService).getDashboardStatistics();
    }

    @Test
    void testGetStudentDistributionStatistics_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("byCollege", Map.of("计算机学院", 50));

        // 模拟服务调用
        when(statisticsService.getStudentDistributionStatistics()).thenReturn(statistics);

        // 执行测试
        mockMvc.perform(get("/statistics/students/distribution")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(statisticsService).getStudentDistributionStatistics();
    }

    @Test
    void testGetDormitoryUsageStatistics_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("occupancyRate", 0.85);

        // 模拟服务调用
        when(statisticsService.getDormitoryUsageStatistics()).thenReturn(statistics);

        // 执行测试
        mockMvc.perform(get("/statistics/dormitories/usage")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(statisticsService).getDormitoryUsageStatistics();
    }

    @Test
    void testGetPaymentStatistics_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalAmount", 10000.0);

        // 模拟服务调用
        when(statisticsService.getPaymentStatistics(anyString(), anyString())).thenReturn(statistics);

        // 执行测试
        mockMvc.perform(get("/statistics/payments")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(statisticsService).getPaymentStatistics(anyString(), anyString());
    }

    @Test
    void testGetRepairStatistics_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRepairs", 50);

        // 模拟服务调用
        when(statisticsService.getRepairStatistics(anyString(), anyString())).thenReturn(statistics);

        // 执行测试
        mockMvc.perform(get("/statistics/repairs")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(statisticsService).getRepairStatistics(null, null);
    }
}

