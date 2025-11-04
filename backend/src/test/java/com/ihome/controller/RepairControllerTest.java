package com.ihome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihome.entity.RepairOrder;
import com.ihome.mapper.RepairOrderMapper;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 维修控制器测试
 */
@WebMvcTest(
    controllers = RepairController.class,
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
public class RepairControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RepairOrderMapper repairOrderMapper;
    
    @MockBean
    private com.ihome.service.NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private RepairOrder testOrder;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testOrder = new RepairOrder();
        testOrder.setId(1);
        testOrder.setStudentId("2024001");
        testOrder.setDormitoryId("D001");
        testOrder.setDescription("水龙头漏水");
        testOrder.setRepairType("水电");
        testOrder.setUrgencyLevel("紧急");
        testOrder.setStatus("待处理");
        testOrder.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateRepairOrder_Success() throws Exception {
        // 准备测试数据（添加studentId，因为SecurityContext为空）
        java.util.Map<String, String> request = new java.util.HashMap<>();
        request.put("studentId", "2024001");
        request.put("dormitoryId", "D001");
        request.put("description", "水龙头漏水");
        request.put("repairType", "水电");
        request.put("urgencyLevel", "紧急");

        // 模拟数据库操作
        when(repairOrderMapper.insert(any(RepairOrder.class))).thenReturn(1);
        Map<String, Object> notificationResult = new HashMap<>();
        notificationResult.put("success", true);
        when(notificationService.sendRepairNotification(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(notificationResult);

        // 执行测试
        mockMvc.perform(post("/repairs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(repairOrderMapper).insert(any(RepairOrder.class));
    }

    @Test
    void testGetStudentRepairs_Success() throws Exception {
        // 准备测试数据
        String studentId = "2024001";
        List<RepairOrder> orders = Arrays.asList(testOrder);

        // 模拟数据库查询（使用MyBatis Plus的selectList）
        when(repairOrderMapper.selectList(any())).thenReturn(orders);

        // 执行测试
        mockMvc.perform(get("/repairs/student/{studentId}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studentId").value("2024001"));

        // 验证调用
        verify(repairOrderMapper).selectList(any());
    }

    @Test
    void testUpdateRepairStatus_Success() throws Exception {
        // 准备测试数据
        Integer orderId = 1;
        String newStatus = "处理中";

        // 模拟数据库查询和更新
        when(repairOrderMapper.selectById(orderId)).thenReturn(testOrder);
        // BaseMapper.update(entity, wrapper) - 第一个参数是实体对象（可以为null），第二个是UpdateWrapper
        // 使用 any() 匹配两个参数，确保能匹配任何调用
        when(repairOrderMapper.update(any(), any())).thenReturn(1);
        
        // Mock NotificationService - 使用 doReturn 确保返回值正确，并且不会调用真实方法
        Map<String, Object> notificationResult = new HashMap<>();
        notificationResult.put("success", true);
        notificationResult.put("message", "通知创建成功");
        // 使用 doReturn 确保mock正确工作，即使方法可能抛出异常
        doReturn(notificationResult).when(notificationService).sendRepairNotification(
            anyString(), 
            eq("2024001"), 
            anyString(), 
            anyString(), 
            anyString()
        );

        // 执行测试（使用请求参数，而不是请求体）
        mockMvc.perform(put("/repairs/{repairId}/status", orderId)  // 注意路径是repairId不是id
                        .param("status", newStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(repairOrderMapper).selectById(orderId);
        verify(repairOrderMapper).update(any(), any());
        // 验证通知服务被调用（因为状态从"待处理"变为"处理中"）
        verify(notificationService, times(1)).sendRepairNotification(
            anyString(), 
            eq("2024001"), 
            anyString(), 
            anyString(), 
            anyString()
        );
    }

    @Test
    void testGetRepairOrder_Success() throws Exception {
        // 准备测试数据
        Integer orderId = 1;

        // 模拟数据库查询
        when(repairOrderMapper.selectById(orderId)).thenReturn(testOrder);

        // 执行测试
        mockMvc.perform(get("/repairs/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.studentId").value("2024001"));

        // 验证调用
        verify(repairOrderMapper).selectById(orderId);
    }

    @Test
    void testGetRepairOrder_NotFound() throws Exception {
        // 准备测试数据
        Integer orderId = 999;

        // 模拟订单不存在
        when(repairOrderMapper.selectById(orderId)).thenReturn(null);

        // 执行测试
        mockMvc.perform(get("/repairs/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("维修工单不存在"));

        // 验证调用
        verify(repairOrderMapper).selectById(orderId);
    }
}

