package com.ihome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihome.entity.PaymentRecord;
import com.ihome.mapper.PaymentRecordMapper;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 支付控制器测试
 */
@WebMvcTest(
    controllers = PaymentController.class,
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
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentRecordMapper paymentRecordMapper;
    
    @MockBean
    private com.ihome.mapper.StudentMapper studentMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentRecord testPayment;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testPayment = new PaymentRecord();
        testPayment.setId(1);
        testPayment.setStudentId("2024001");
        testPayment.setAmount(new BigDecimal("100.00"));
        testPayment.setPaymentMethod("微信支付");
        testPayment.setPaymentTime(LocalDateTime.now());
        // PaymentRecord没有status字段
    }

    @Test
    void testCreatePayment_Success() throws Exception {
        // 准备测试数据
        PaymentController.CreatePaymentRequest request = new PaymentController.CreatePaymentRequest();
        request.setStudentId("2024001");
        request.setAmount(new BigDecimal("100.00"));
        request.setPaymentMethod("微信支付");

        // 模拟数据库操作
        com.ihome.entity.Student testStudent = new com.ihome.entity.Student();
        testStudent.setId("2024001");
        when(studentMapper.selectById("2024001")).thenReturn(testStudent);
        when(paymentRecordMapper.insert(any(PaymentRecord.class))).thenReturn(1);

        // 执行测试
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(studentMapper).selectById("2024001");
        verify(paymentRecordMapper).insert(any(PaymentRecord.class));
    }

    @Test
    void testGetStudentPayments_Success() throws Exception {
        // 准备测试数据
        String studentId = "2024001";
        List<PaymentRecord> payments = Arrays.asList(testPayment);

        // 模拟数据库查询（使用MyBatis Plus的selectList）
        when(paymentRecordMapper.selectList(any())).thenReturn(payments);

        // 执行测试
        mockMvc.perform(get("/payments/student/{studentId}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studentId").value("2024001"));

        // 验证调用
        verify(paymentRecordMapper).selectList(any());
    }

    @Test
    void testUpdatePaymentStatus_Success() throws Exception {
        // 准备测试数据
        Integer paymentId = 1;
        String newStatus = "已退款";

        // 模拟数据库查询和更新
        when(paymentRecordMapper.selectById(paymentId)).thenReturn(testPayment);
        when(paymentRecordMapper.updateById(any(PaymentRecord.class))).thenReturn(1);

        // 执行测试（使用请求参数）
        mockMvc.perform(put("/payments/{id}/status", paymentId)
                        .param("status", newStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(paymentRecordMapper).selectById(paymentId);
        verify(paymentRecordMapper).updateById(any(PaymentRecord.class));
    }

    @Test
    void testGetPayment_Success() throws Exception {
        // 准备测试数据
        Integer paymentId = 1;

        // 模拟数据库查询
        when(paymentRecordMapper.selectById(paymentId)).thenReturn(testPayment);

        // 执行测试
        mockMvc.perform(get("/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.studentId").value("2024001"))
                .andExpect(jsonPath("$.data.amount").value(100.00));

        // 验证调用
        verify(paymentRecordMapper).selectById(paymentId);
    }
}

