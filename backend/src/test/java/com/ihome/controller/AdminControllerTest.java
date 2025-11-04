package com.ihome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihome.common.JwtUtils;
import com.ihome.entity.Admin;
import com.ihome.entity.Student;
import com.ihome.entity.Dormitory;
import com.ihome.mapper.*;
import com.ihome.service.StatisticsService;
import com.ihome.service.StudentImportService;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 管理员控制器测试
 */
@WebMvcTest(
    controllers = AdminController.class,
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
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminMapper adminMapper;

    @MockBean
    private StudentMapper studentMapper;

    @MockBean
    private DormitoryMapper dormitoryMapper;

    @MockBean
    private BedMapper bedMapper;

    @MockBean
    private DormitoryAllocationMapper allocationMapper;

    @MockBean
    private NotificationMapper notificationMapper;

    @MockBean
    private RepairOrderMapper repairOrderMapper;

    @MockBean
    private PaymentRecordMapper paymentRecordMapper;

    @MockBean
    private DormitorySwitchMapper switchMapper;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private StatisticsService statisticsService;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private StudentImportService studentImportService;

    @Autowired
    private ObjectMapper objectMapper;

    private Admin testAdmin;
    private Student testStudent;
    private Dormitory testDormitory;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 创建测试管理员
        testAdmin = new Admin();
        testAdmin.setId("admin001");
        testAdmin.setName("系统管理员");
        testAdmin.setPassword("$2a$10$encodedPassword");
        testAdmin.setEmail("admin@example.com");
        testAdmin.setPhoneNumber("13800138000");
        testAdmin.setAdminType("admin");

        // 创建测试学生
        testStudent = new Student();
        testStudent.setId("2024001");
        testStudent.setName("张三");
        testStudent.setPassword("$2a$10$encodedPassword");
        testStudent.setGender("男");
        testStudent.setCollege("计算机学院");
        testStudent.setMajor("计算机科学");
        testStudent.setGrade("2024");
        testStudent.setStatus("在校");

        // 创建测试宿舍
        testDormitory = new Dormitory();
        testDormitory.setId("D001");
        testDormitory.setBuildingId("B001");
        testDormitory.setFloorNumber(1);
        testDormitory.setRoomNumber("101");
        testDormitory.setRoomType("四人间");
        testDormitory.setBedCount(4);
        testDormitory.setCurrentOccupancy(0);
        testDormitory.setStatus("可用");
    }

    @Test
    void testAdminLogin_Success() throws Exception {
        // 准备测试数据
        String adminId = "admin001";
        String password = "password";
        String accessToken = "admin-access-token";
        String refreshToken = "admin-refresh-token";

        // 模拟数据库查询
        when(adminMapper.selectById(adminId)).thenReturn(testAdmin);
        when(passwordEncoder.matches(password, testAdmin.getPassword())).thenReturn(true);
        when(jwtUtils.generateAccessToken(adminId, "admin")).thenReturn(accessToken);
        when(jwtUtils.generateRefreshToken(adminId, "admin")).thenReturn(refreshToken);

        // 准备请求体
        Map<String, String> request = new HashMap<>();
        request.put("id", adminId);
        request.put("password", password);

        // 执行测试
        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value(accessToken))
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken));

        // 验证调用
        verify(adminMapper).selectById(adminId);
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(jwtUtils).generateAccessToken(adminId, "admin");
    }

    @Test
    void testAdminLogin_InvalidCredentials() throws Exception {
        // 准备测试数据
        String adminId = "admin001";
        String password = "wrong-password";

        // 模拟数据库查询
        when(adminMapper.selectById(adminId)).thenReturn(testAdmin);
        when(passwordEncoder.matches(password, testAdmin.getPassword())).thenReturn(false);

        // 准备请求体
        Map<String, String> request = new HashMap<>();
        request.put("id", adminId);
        request.put("password", password);

        // 执行测试
        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("管理员账号或密码错误"));

        // 验证调用
        verify(adminMapper).selectById(adminId);
        verify(passwordEncoder).matches(password, testAdmin.getPassword());
        verify(jwtUtils, never()).generateAccessToken(anyString(), anyString());
    }

    @Test
    void testGetDashboard_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalStudents", 100);
        statistics.put("totalDormitories", 50);
        statistics.put("totalAllocations", 80);
        statistics.put("totalRepairs", 20);

        // 模拟服务调用
        when(statisticsService.getDashboardStatistics()).thenReturn(statistics);
        when(allocationMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(repairOrderMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(switchMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(paymentRecordMapper.selectList(any())).thenReturn(new ArrayList<>());

        // 执行测试
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalStudents").value(100))
                .andExpect(jsonPath("$.data.totalDormitories").value(50));

        // 验证调用
        verify(statisticsService).getDashboardStatistics();
    }

    @Test
    void testGetStudents_Success() throws Exception {
        // 准备测试数据
        List<Student> students = Arrays.asList(testStudent);

        // 模拟数据库查询
        when(studentMapper.selectList(null)).thenReturn(students);

        // 执行测试
        mockMvc.perform(get("/admin/students")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        // 验证调用
        verify(studentMapper).selectList(null);
    }

    @Test
    void testGetStudents_WithSearch() throws Exception {
        // 准备测试数据
        List<Student> students = Arrays.asList(testStudent);

        // 模拟数据库查询
        when(studentMapper.selectList(null)).thenReturn(students);

        // 执行测试（带搜索参数）
        mockMvc.perform(get("/admin/students")
                        .param("page", "1")
                        .param("size", "10")
                        .param("name", "张三"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(studentMapper).selectList(null);
    }

    @Test
    void testCreateStudent_Success() throws Exception {
        // 准备测试数据
        Map<String, String> request = new HashMap<>();
        request.put("id", "2024002");
        request.put("name", "李四");
        request.put("password", "password");
        request.put("gender", "女");
        request.put("college", "软件学院");
        request.put("major", "软件工程");
        request.put("grade", "2024");

        // 模拟数据库操作
        when(studentMapper.selectById("2024002")).thenReturn(null);
        when(passwordEncoder.encode("password")).thenReturn("$2a$10$encodedPassword");
        when(studentMapper.insert(any(Student.class))).thenReturn(1);

        // 执行测试
        mockMvc.perform(post("/admin/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(studentMapper).selectById("2024002");
        verify(passwordEncoder).encode("password");
        verify(studentMapper).insert(any(Student.class));
    }

    @Test
    void testCreateStudent_DuplicateId() throws Exception {
        // 准备测试数据
        Map<String, String> request = new HashMap<>();
        request.put("id", "2024001");
        request.put("name", "李四");

        // 模拟学生已存在
        when(studentMapper.selectById("2024001")).thenReturn(testStudent);

        // 执行测试
        mockMvc.perform(post("/admin/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("学号已存在"));

        // 验证调用
        verify(studentMapper).selectById("2024001");
        verify(studentMapper, never()).insert(any(Student.class));
    }

    @Test
    void testUpdateStudent_Success() throws Exception {
        // 准备测试数据
        String studentId = "2024001";
        Map<String, String> request = new HashMap<>();
        request.put("name", "李四");
        request.put("phoneNumber", "13900139000");

        // 模拟数据库更新
        when(studentMapper.updateById(any(Student.class))).thenReturn(1);

        // 执行测试
        mockMvc.perform(put("/admin/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(studentMapper).updateById(any(Student.class));
    }

    @Test
    void testGetDormitories_Success() throws Exception {
        // 准备测试数据
        List<Dormitory> dormitories = Arrays.asList(testDormitory);

        // 模拟数据库查询
        when(dormitoryMapper.selectList(any())).thenReturn(dormitories);

        // 执行测试
        mockMvc.perform(get("/admin/dormitories")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        // 验证调用
        verify(dormitoryMapper).selectList(any());
    }

    @Test
    void testCreateDormitory_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> request = new HashMap<>();
        request.put("id", "D002");
        request.put("buildingId", "B001");
        request.put("floorNumber", 2);
        request.put("roomNumber", "201");
        request.put("roomType", "四人间");
        request.put("bedCount", 4);

        // 模拟数据库操作
        when(dormitoryMapper.selectById("D002")).thenReturn(null);
        when(dormitoryMapper.insert(any(Dormitory.class))).thenReturn(1);

        // 执行测试
        mockMvc.perform(post("/admin/dormitories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(dormitoryMapper).selectById("D002");
        verify(dormitoryMapper).insert(any(Dormitory.class));
    }
}

