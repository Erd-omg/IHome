package com.ihome.controller;

import com.ihome.entity.DormitoryAllocation;
import com.ihome.entity.Student;
import com.ihome.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.ihome.config.TestWebSecurityConfig;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 调换推荐控制器测试
 */
@WebMvcTest(
    controllers = ExchangeRecommendationController.class,
    excludeAutoConfiguration = {
        UserDetailsServiceAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        SqlInitializationAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        MybatisPlusAutoConfiguration.class
    }
)
@Import(TestWebSecurityConfig.class)
@ActiveProfiles("test")
public class ExchangeRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentMapper studentMapper;

    @MockBean
    private DormitoryAllocationMapper allocationMapper;

    @MockBean
    private QuestionnaireAnswerMapper questionnaireMapper;

    @MockBean
    private RoommateTagMapper roommateTagMapper;

    @MockBean
    private BedMapper bedMapper;

    private Student testStudent;
    private DormitoryAllocation testAllocation;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testStudent = new Student();
        testStudent.setId("2024001");
        testStudent.setName("张三");
        testStudent.setMajor("计算机科学");
        testStudent.setCollege("计算机学院");

        testAllocation = new DormitoryAllocation();
        testAllocation.setId(1);
        testAllocation.setStudentId("2024001");
        testAllocation.setBedId("B001");
        testAllocation.setCheckInDate(LocalDate.now());
        testAllocation.setStatus("在住");
    }

    @Test
    void testGetRecommendations_Success() throws Exception {
        // 模拟数据库查询
        // 使用调用顺序来区分不同的selectList调用
        // 第一次：getCurrentAllocation -> 查询student_id="2024001"和status="在住"
        // 第二次：getRoommateIds -> 查询bed_id="B001"和status="在住"
        when(allocationMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper.class)))
            .thenReturn(Arrays.asList(testAllocation))  // getCurrentAllocation
            .thenReturn(Arrays.asList());  // getRoommateIds (返回空，表示没有室友)
        
        // getRoommateIds需要bedMapper.selectById
        com.ihome.entity.Bed testBed = new com.ihome.entity.Bed();
        testBed.setId("B001");
        testBed.setDormitoryId("D001");
        when(bedMapper.selectById("B001")).thenReturn(testBed);
        
        // calculateCompatibility需要studentMapper.selectById
        when(studentMapper.selectById("2024001")).thenReturn(testStudent);
        Student anotherStudent = new Student();
        anotherStudent.setId("2024002");
        anotherStudent.setName("李四");
        anotherStudent.setMajor("计算机科学");
        anotherStudent.setCollege("计算机学院");
        when(studentMapper.selectById("2024002")).thenReturn(anotherStudent);
        
        // studentMapper.selectList返回包含候选学生的列表
        when(studentMapper.selectList(null)).thenReturn(Arrays.asList(testStudent, anotherStudent));
        
        // calculateCompatibility需要的mocks
        when(questionnaireMapper.selectByStudentId("2024001")).thenReturn(null);
        when(questionnaireMapper.selectByStudentId("2024002")).thenReturn(null);
        // calculateTagCompatibility会调用两次roommateTagMapper.selectList（一次查询studentId1，一次查询studentId2）
        when(roommateTagMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper.class)))
            .thenReturn(Arrays.asList())  // 第一次调用（studentId1）
            .thenReturn(Arrays.asList());  // 第二次调用（studentId2）
        
        // generateRecommendationReason也需要studentMapper.selectById
        // 已经在上面配置了

        // 执行测试
        mockMvc.perform(get("/exchange/recommendations/for-student")
                        .param("studentId", "2024001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(allocationMapper, atLeastOnce()).selectList(any());
        verify(studentMapper).selectList(null);
    }

    @Test
    void testGetRecommendations_NoAllocation() throws Exception {
        // 模拟没有分配记录 - getCurrentAllocation返回空列表
        when(allocationMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper.class)))
            .thenReturn(Arrays.asList());  // getCurrentAllocation返回空

        // 执行测试
        mockMvc.perform(get("/exchange/recommendations/for-student")
                        .param("studentId", "2024001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("您还没有分配宿舍"));

        verify(allocationMapper).selectList(any());
    }
}

