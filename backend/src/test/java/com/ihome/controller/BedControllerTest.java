package com.ihome.controller;

import com.ihome.entity.Bed;
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
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 床位控制器测试
 */
@WebMvcTest(
    controllers = BedController.class,
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
public class BedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BedMapper bedMapper;

    @MockBean
    private DormitoryAllocationMapper allocationMapper;

    @MockBean
    private StudentMapper studentMapper;

    @MockBean
    private RoommateTagMapper roommateTagMapper;

    private Bed testBed;
    private DormitoryAllocation testAllocation;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testBed = new Bed();
        testBed.setId("B001");
        testBed.setDormitoryId("D001");
        testBed.setBedType("下铺");
        testBed.setStatus("可用");

        testAllocation = new DormitoryAllocation();
        testAllocation.setId(1);
        testAllocation.setStudentId("2024001");
        testAllocation.setBedId("B001");
        testAllocation.setCheckInDate(LocalDate.now());
        testAllocation.setStatus("在住");

        testStudent = new Student();
        testStudent.setId("2024001");
        testStudent.setName("张三");
    }

    @Test
    void testGetCurrentBed_Success() throws Exception {
        // 模拟数据库查询
        when(allocationMapper.selectList(any())).thenReturn(Arrays.asList(testAllocation));
        when(bedMapper.selectById("B001")).thenReturn(testBed);

        // 执行测试
        mockMvc.perform(get("/beds/current")
                        .param("studentId", "2024001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bedId").value("B001"));

        verify(allocationMapper).selectList(any());
        verify(bedMapper).selectById("B001");
    }

    @Test
    void testGetCurrentBed_NoAllocation() throws Exception {
        // 模拟没有分配记录
        when(allocationMapper.selectList(any())).thenReturn(Arrays.asList());

        mockMvc.perform(get("/beds/current")
                        .param("studentId", "2024001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(allocationMapper).selectList(any());
    }

    @Test
    void testGetRecommendedBeds_Success() throws Exception {
        // 模拟数据库查询
        when(studentMapper.selectById("2024001")).thenReturn(testStudent);
        when(roommateTagMapper.selectList(any())).thenReturn(Arrays.asList());
        when(bedMapper.selectList(any())).thenReturn(Arrays.asList(testBed));

        mockMvc.perform(get("/beds/recommended")
                        .param("studentId", "2024001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(studentMapper).selectById(anyString());
        verify(bedMapper).selectList(any());
    }

    @Test
    void testGetAvailableBeds_Success() throws Exception {
        // 模拟数据库查询
        when(bedMapper.selectList(any())).thenReturn(Arrays.asList(testBed));

        mockMvc.perform(get("/beds/available")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(bedMapper).selectList(any());
    }

    @Test
    void testSearchBeds_Success() throws Exception {
        // 模拟数据库查询
        when(bedMapper.selectList(any())).thenReturn(Arrays.asList(testBed));

        mockMvc.perform(get("/beds/search")
                        .param("keyword", "D001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(bedMapper).selectList(any());
    }

    @Test
    void testSelectBed_Success() throws Exception {
        // 模拟数据库查询和操作
        when(bedMapper.selectById("B001")).thenReturn(testBed);
        when(allocationMapper.selectList(any())).thenReturn(Arrays.asList());
        when(allocationMapper.insert(any(DormitoryAllocation.class))).thenReturn(1);
        when(bedMapper.updateById(any(Bed.class))).thenReturn(1);

        mockMvc.perform(post("/beds/select")
                        .param("bedId", "B001")
                        .param("studentId", "2024001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(bedMapper).selectById("B001");
        verify(allocationMapper).insert(any(DormitoryAllocation.class));
    }

    @Test
    void testSelectBed_BedNotFound() throws Exception {
        // 模拟床位不存在
        when(bedMapper.selectById("B999")).thenReturn(null);

        mockMvc.perform(post("/beds/select")
                        .param("bedId", "B999")
                        .param("studentId", "2024001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("床位不存在"));

        verify(bedMapper).selectById("B999");
        verify(allocationMapper, never()).insert(any(DormitoryAllocation.class));
    }

    @Test
    void testSelectBed_BedOccupied() throws Exception {
        // 模拟床位已被占用
        Bed occupiedBed = new Bed();
        occupiedBed.setId("B001");
        occupiedBed.setStatus("已占用");

        when(bedMapper.selectById("B001")).thenReturn(occupiedBed);

        mockMvc.perform(post("/beds/select")
                        .param("bedId", "B001")
                        .param("studentId", "2024001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("床位不可用"));

        verify(bedMapper).selectById("B001");
        verify(allocationMapper, never()).insert(any(DormitoryAllocation.class));
    }
}

