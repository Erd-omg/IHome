package com.ihome.controller;

import com.ihome.entity.DormitoryAllocation;
import com.ihome.mapper.DormitoryAllocationMapper;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 分配控制器测试
 */
@WebMvcTest(
    controllers = AllocationController.class,
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
public class AllocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DormitoryAllocationMapper allocationMapper;

    private DormitoryAllocation testAllocation;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testAllocation = new DormitoryAllocation();
        testAllocation.setId(1);
        testAllocation.setStudentId("2024001");
        testAllocation.setBedId("B001");
        testAllocation.setDormitoryId("D001");
        testAllocation.setCheckInDate(LocalDate.now());
        testAllocation.setStatus("在住");
    }

    @Test
    void testGetCurrentAllocation_Success() throws Exception {
        // 模拟数据库查询
        when(allocationMapper.selectOne(any())).thenReturn(testAllocation);

        // 执行测试
        mockMvc.perform(get("/allocations/student/{studentId}", "2024001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value("2024001"))
                .andExpect(jsonPath("$.data.bedId").value("B001"));

        verify(allocationMapper).selectOne(any());
    }

    @Test
    void testGetCurrentAllocation_NotInDorm() throws Exception {
        // 模拟未在住
        when(allocationMapper.selectOne(any())).thenReturn(null);

        // 执行测试
        mockMvc.perform(get("/allocations/student/{studentId}", "2024001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("当前未在住"));

        verify(allocationMapper).selectOne(any());
    }

    @Test
    void testGetAllocationHistory_Success() throws Exception {
        // 准备测试数据
        List<DormitoryAllocation> allocations = Arrays.asList(testAllocation);

        // 模拟数据库查询
        when(allocationMapper.selectList(any())).thenReturn(allocations);

        // 执行测试
        mockMvc.perform(get("/allocations/student/{studentId}/history", "2024001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(allocationMapper).selectList(any());
    }
}

