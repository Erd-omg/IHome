package com.ihome.controller;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 智能分配控制器测试
 */
@WebMvcTest(
    controllers = IntelligentAllocationController.class,
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
public class IntelligentAllocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AllocationService allocationService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 测试前准备
    }

    @Test
    void testIntelligentAllocation_Success() throws Exception {
        // 准备测试数据
        List<String> studentIds = Arrays.asList("2024001", "2024002");
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("allocations", Arrays.asList());
        serviceResult.put("totalAllocated", 2);
        serviceResult.put("totalUnallocated", 0);

        // 模拟服务调用
        when(allocationService.intelligentAllocation(anyList())).thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(post("/allocation/intelligent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentIds))
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAllocated").value(2));

        verify(allocationService).intelligentAllocation(anyList());
    }

    @Test
    void testIntelligentAllocation_EmptyList() throws Exception {
        // 准备测试数据
        List<String> studentIds = Arrays.asList();

        // 执行测试
        mockMvc.perform(post("/allocation/intelligent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentIds))
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("学生ID列表不能为空"));

        verify(allocationService, never()).intelligentAllocation(anyList());
    }

    @Test
    void testGetAllocationSuggestions_Success() throws Exception {
        // 准备测试数据
        String studentId = "2024001";
        Map<String, Object> suggestions = new HashMap<>();
        suggestions.put("suggestions", Arrays.asList());
        suggestions.put("studentMajor", "计算机科学");

        // 模拟服务调用
        when(allocationService.getAllocationSuggestions(studentId)).thenReturn(suggestions);

        // 执行测试
        mockMvc.perform(get("/allocation/suggestions/{studentId}", studentId)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(allocationService).getAllocationSuggestions(studentId);
    }

    @Test
    void testBatchAllocation_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> request = new HashMap<>();
        request.put("studentIds", Arrays.asList("2024001", "2024002"));
        request.put("dormitoryId", "D001");

        // 执行测试
        mockMvc.perform(post("/allocation/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetAllocationStatistics_Success() throws Exception {
        // 执行测试
        mockMvc.perform(get("/allocation/statistics")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

