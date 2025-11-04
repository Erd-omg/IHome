package com.ihome.controller;

import com.ihome.entity.Dormitory;
import com.ihome.entity.DormitoryAllocation;
import com.ihome.entity.Student;
import com.ihome.entity.Bed;
import com.ihome.mapper.*;
import com.ihome.service.DormitoryService;
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
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 宿舍控制器测试
 */
@WebMvcTest(
    controllers = DormitoryController.class,
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
public class DormitoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DormitoryMapper dormitoryMapper;

    @MockBean
    private DormitoryAllocationMapper allocationMapper;

    @MockBean
    private StudentMapper studentMapper;

    @MockBean
    private BedMapper bedMapper;

    @MockBean
    private DormitoryService dormitoryService;

    private Dormitory testDormitory;
    private DormitoryAllocation testAllocation;
    private Bed testBed;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 创建测试宿舍
        testDormitory = new Dormitory();
        testDormitory.setId("D001");
        testDormitory.setBuildingId("B001");
        testDormitory.setFloorNumber(1);
        testDormitory.setRoomNumber("101");
        testDormitory.setRoomType("四人间");
        testDormitory.setBedCount(4);
        testDormitory.setCurrentOccupancy(2);
        testDormitory.setStatus("可用");

        // 创建测试分配
        testAllocation = new DormitoryAllocation();
        testAllocation.setId(1);
        testAllocation.setStudentId("2024001");
        testAllocation.setBedId("B001");
        testAllocation.setDormitoryId("D001");
        testAllocation.setCheckInDate(LocalDate.now());
        testAllocation.setStatus("在住");

        // 创建测试床位
        testBed = new Bed();
        testBed.setId("B001");
        testBed.setDormitoryId("D001");
        testBed.setBedType("下铺");
        testBed.setStatus("已占用");

        // 创建测试学生
        testStudent = new Student();
        testStudent.setId("2024001");
        testStudent.setName("张三");
        testStudent.setGender("男");
        testStudent.setCollege("计算机学院");
        testStudent.setMajor("计算机科学");
    }

    @Test
    void testListDorms_Success() throws Exception {
        // 准备测试数据
        List<Dormitory> dormitories = Arrays.asList(testDormitory);

        // 模拟数据库查询
        when(dormitoryMapper.selectList(any())).thenReturn(dormitories);

        // 执行测试
        mockMvc.perform(get("/dorms")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        // 验证调用
        verify(dormitoryMapper).selectList(any());
    }

    @Test
    void testListDorms_WithFilters() throws Exception {
        // 准备测试数据
        List<Dormitory> dormitories = Arrays.asList(testDormitory);

        // 模拟数据库查询
        when(dormitoryMapper.selectList(any())).thenReturn(dormitories);

        // 执行测试（带过滤条件）
        mockMvc.perform(get("/dorms")
                        .param("buildingId", "B001")
                        .param("status", "可用")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(dormitoryMapper).selectList(any());
    }

    @Test
    void testGetDormitoryDetail_Success() throws Exception {
        // 准备测试数据
        String dormitoryId = "D001";
        List<DormitoryAllocation> allocations = Arrays.asList(testAllocation);
        List<Bed> beds = Arrays.asList(testBed);

        // 模拟数据库查询
        when(dormitoryMapper.selectById(dormitoryId)).thenReturn(testDormitory);
        when(allocationMapper.selectList(any())).thenReturn(allocations);  // controller使用selectList而不是selectByDormitoryId
        when(bedMapper.selectList(any())).thenReturn(beds);
        when(studentMapper.selectById("2024001")).thenReturn(testStudent);

        // 执行测试
        mockMvc.perform(get("/dorms/{dormitoryId}/detail", dormitoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("D001"))
                .andExpect(jsonPath("$.data.beds").isArray());

        // 验证调用
        verify(dormitoryMapper).selectById(dormitoryId);
        verify(allocationMapper).selectList(any());  // 验证selectList被调用
    }

    @Test
    void testGetDormitoryDetail_NotFound() throws Exception {
        // 准备测试数据
        String dormitoryId = "D999";

        // 模拟宿舍不存在
        when(dormitoryMapper.selectById(dormitoryId)).thenReturn(null);

        // 执行测试
        mockMvc.perform(get("/dorms/{dormitoryId}/detail", dormitoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("宿舍不存在"));

        // 验证调用
        verify(dormitoryMapper).selectById(dormitoryId);
        verify(allocationMapper, never()).selectByDormitoryId(anyString());
    }

    @Test
    void testSearchBeds_Success() throws Exception {
        // 准备测试数据 - 测试 /dorms/{dormitoryId}/beds 路径
        String dormitoryId = "D001";
        List<Bed> beds = Arrays.asList(testBed);
        
        List<Map<String, Object>> bedInfoList = new ArrayList<>();
        Map<String, Object> bedInfo = new HashMap<>();
        bedInfo.put("id", testBed.getId());
        bedInfo.put("dormitoryId", testBed.getDormitoryId());
        bedInfo.put("bedNumber", testBed.getBedNumber());
        bedInfo.put("bedType", testBed.getBedType());
        bedInfo.put("status", testBed.getStatus());
        bedInfoList.add(bedInfo);

        // 模拟数据库查询
        when(bedMapper.selectList(any())).thenReturn(beds);
        when(allocationMapper.selectList(any())).thenReturn(new ArrayList<>());

        // 执行测试 - 使用正确的路径 /dorms/{dormitoryId}/beds
        mockMvc.perform(get("/dorms/{dormitoryId}/beds", dormitoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // 验证调用
        verify(bedMapper).selectList(any());
    }

    @Test
    void testListBeds_Success() throws Exception {
        // 准备测试数据
        String dormitoryId = "D001";
        List<Bed> beds = Arrays.asList(testBed);

        // 模拟数据库查询
        when(bedMapper.selectList(any())).thenReturn(beds);

        // 执行测试
        mockMvc.perform(get("/dorms/{dormitoryId}/beds", dormitoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        // 验证调用
        verify(bedMapper).selectList(any());
    }
}

