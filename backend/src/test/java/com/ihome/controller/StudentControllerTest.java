package com.ihome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihome.common.JwtUtils;
import com.ihome.entity.Student;
import com.ihome.entity.DormitoryAllocation;
import com.ihome.entity.Notification;
import com.ihome.entity.PaymentRecord;
import com.ihome.mapper.*;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 学生控制器测试
 */
@WebMvcTest(
    controllers = StudentController.class,
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
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentMapper studentMapper;

    @MockBean
    private DormitoryAllocationMapper allocationMapper;

    @MockBean
    private NotificationMapper notificationMapper;

    @MockBean
    private PaymentRecordMapper paymentRecordMapper;

    @MockBean
    private BedMapper bedMapper;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private Student testStudent;
    private DormitoryAllocation testAllocation;
    private Notification testNotification;
    private PaymentRecord testPayment;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 创建测试学生
        testStudent = new Student();
        testStudent.setId("2024001");
        testStudent.setName("张三");
        testStudent.setPassword("$2a$10$encodedPassword");
        testStudent.setPhoneNumber("13800138000");
        testStudent.setEmail("test@example.com");
        testStudent.setGender("男");
        testStudent.setCollege("计算机学院");
        testStudent.setMajor("计算机科学");
        testStudent.setGrade("2024");
        testStudent.setStatus("在校");

        // 创建测试分配
        testAllocation = new DormitoryAllocation();
        testAllocation.setId(1);
        testAllocation.setStudentId("2024001");
        testAllocation.setBedId("B001");
        testAllocation.setDormitoryId("D001");
        testAllocation.setCheckInDate(LocalDate.now());
        testAllocation.setStatus("在住");

        // 创建测试通知
        testNotification = new Notification();
        testNotification.setId(1);
        testNotification.setTitle("测试通知");
        testNotification.setContent("这是一条测试通知");
        testNotification.setCreateTime(LocalDateTime.now());
        testNotification.setReceiverId("2024001");
        testNotification.setReceiverType("student");

        // 创建测试支付记录
        testPayment = new PaymentRecord();
        testPayment.setId(1);
        testPayment.setStudentId("2024001");
        testPayment.setAmount(java.math.BigDecimal.valueOf(100.00));
        testPayment.setPaymentTime(LocalDateTime.now());
    }

    @Test
    void testLogin_Success() throws Exception {
        // 准备测试数据
        String studentId = "2024001";
        String password = "password";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        // 模拟数据库查询
        when(studentMapper.selectById(studentId)).thenReturn(testStudent);
        when(passwordEncoder.matches(password, testStudent.getPassword())).thenReturn(true);
        when(jwtUtils.generateAccessToken(studentId, "student")).thenReturn(accessToken);
        when(jwtUtils.generateRefreshToken(studentId, "student")).thenReturn(refreshToken);

        // 准备请求体
        StudentController.LoginRequest request = new StudentController.LoginRequest();
        request.setId(studentId);
        request.setPassword(password);

        // 执行测试
        mockMvc.perform(post("/students/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value(accessToken))
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken));

        // 验证调用
        verify(studentMapper).selectById(studentId);
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(jwtUtils).generateAccessToken(studentId, "student");
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // 准备测试数据
        String studentId = "2024001";
        String password = "wrong-password";

        // 模拟数据库查询
        when(studentMapper.selectById(studentId)).thenReturn(testStudent);
        when(passwordEncoder.matches(password, testStudent.getPassword())).thenReturn(false);

        // 准备请求体
        StudentController.LoginRequest request = new StudentController.LoginRequest();
        request.setId(studentId);
        request.setPassword(password);

        // 执行测试
        mockMvc.perform(post("/students/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("学号或密码错误"));

        // 验证调用
        verify(studentMapper).selectById(studentId);
        verify(passwordEncoder).matches(password, testStudent.getPassword());
        verify(jwtUtils, never()).generateAccessToken(anyString(), anyString());
    }

    @Test
    void testLogin_StudentNotFound() throws Exception {
        // 准备测试数据
        String studentId = "9999999";
        String password = "password";

        // 模拟学生不存在
        when(studentMapper.selectById(studentId)).thenReturn(null);

        // 准备请求体
        StudentController.LoginRequest request = new StudentController.LoginRequest();
        request.setId(studentId);
        request.setPassword(password);

        // 执行测试
        mockMvc.perform(post("/students/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("学号或密码错误"));

        // 验证调用
        verify(studentMapper).selectById(studentId);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testGetMyDormitory_Success() throws Exception {
        // 准备测试数据
        String studentId = "2024001";

        // 模拟数据库查询 - 使用QueryWrapper返回列表
        List<DormitoryAllocation> allocations = Arrays.asList(testAllocation);
        when(allocationMapper.selectList(any())).thenReturn(allocations);

        // 模拟床位查询
        com.ihome.entity.Bed testBed = new com.ihome.entity.Bed();
        testBed.setId("B001");
        testBed.setDormitoryId("D001");
        testBed.setBedNumber("1");
        testBed.setBedType("下铺");
        when(bedMapper.selectById(testAllocation.getBedId())).thenReturn(testBed);
        
        // 执行测试
        mockMvc.perform(get("/students/{id}/current-allocation", studentId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value("2024001"))
                .andExpect(jsonPath("$.data.dormitoryId").value("D001"));

        // 验证调用
        verify(allocationMapper).selectList(any());
    }

    @Test
    void testGetMyNotifications_Success() throws Exception {
        // 准备测试数据
        String studentId = "2024001";
        List<Notification> notifications = Arrays.asList(testNotification);

        // 模拟数据库查询 - 使用QueryWrapper返回列表
        when(notificationMapper.selectList(any())).thenReturn(notifications);

        // 执行测试
        mockMvc.perform(get("/students/{id}/notifications", studentId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        // 验证调用
        verify(notificationMapper).selectList(any());
    }

    @Test
    void testGetMyPayments_Success() throws Exception {
        // 准备测试数据
        String studentId = "2024001";
        List<PaymentRecord> payments = Arrays.asList(testPayment);

        // 模拟数据库查询（使用MyBatis Plus的selectList）
        when(paymentRecordMapper.selectList(any())).thenReturn(payments);

        // 执行测试
        mockMvc.perform(get("/students/{id}/payments", studentId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        // 验证调用
        verify(paymentRecordMapper).selectList(any());
    }

    @Test
    void testUpdateProfile_Success() throws Exception {
        // 准备测试数据
        String studentId = "2024001";
        Student updatedStudent = new Student();
        updatedStudent.setId(studentId);
        updatedStudent.setName("李四");
        updatedStudent.setPhoneNumber("13900139000");
        updatedStudent.setEmail("newemail@example.com");

        // 模拟数据库查询和更新
        when(studentMapper.selectById(studentId)).thenReturn(testStudent);
        when(studentMapper.updateById(any(Student.class))).thenReturn(1);

        // 准备请求体（使用Map，因为UpdateProfileRequest可能是内部类）
        java.util.Map<String, String> request = new java.util.HashMap<>();
        request.put("name", "李四");
        request.put("phoneNumber", "13900139000");
        request.put("email", "newemail@example.com");

        // 执行测试
        mockMvc.perform(put("/students/{id}", studentId)
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(studentMapper).selectById(studentId);
        verify(studentMapper).updateById(any(Student.class));
    }
}

