package com.ihome.service;

import com.ihome.entity.Bed;
import com.ihome.entity.Dormitory;
import com.ihome.entity.DormitoryAllocation;
import com.ihome.mapper.BedMapper;
import com.ihome.mapper.DormitoryAllocationMapper;
import com.ihome.mapper.DormitoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 宿舍服务测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DormitoryServiceTest {

    @Mock
    private DormitoryMapper dormitoryMapper;

    @Mock
    private BedMapper bedMapper;

    @Mock
    private DormitoryAllocationMapper allocationMapper;

    @InjectMocks
    private DormitoryService dormitoryService;

    private Dormitory testDormitory;
    private Bed testBed;
    private DormitoryAllocation testAllocation;

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
        testDormitory.setCurrentOccupancy(0);
        testDormitory.setStatus("可用");

        // 创建测试床位
        testBed = new Bed();
        testBed.setId("B001");
        testBed.setDormitoryId("D001");
        testBed.setBedType("下铺");
        testBed.setStatus("可用");

        // 创建测试分配
        testAllocation = new DormitoryAllocation();
        testAllocation.setId(1);
        testAllocation.setStudentId("2024001");
        testAllocation.setBedId("B001");
        testAllocation.setDormitoryId("D001");
        testAllocation.setCheckInDate(LocalDate.now());
        testAllocation.setStatus("在住");
    }

    @Test
    void testListDormitories_Success() {
        // 准备测试数据
        List<Dormitory> expectedDormitories = Arrays.asList(testDormitory);

        // 模拟数据库查询
        when(dormitoryMapper.selectList(any())).thenReturn(expectedDormitories);

        // 执行测试
        List<Dormitory> result = dormitoryService.listDormitories("B001", "可用");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("D001", result.get(0).getId());

        // 验证调用
        verify(dormitoryMapper).selectList(any());
    }

    @Test
    void testListBeds_Success() {
        // 准备测试数据
        String dormitoryId = "D001";
        List<Bed> expectedBeds = Arrays.asList(testBed);

        // 模拟数据库查询
        when(bedMapper.selectList(any())).thenReturn(expectedBeds);

        // 执行测试
        List<Bed> result = dormitoryService.listBeds(dormitoryId, "可用");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("B001", result.get(0).getId());

        // 验证调用
        verify(bedMapper).selectList(any());
    }

    @Test
    void testChooseBed_Success() {
        // 准备测试数据
        String studentId = "2024001";
        String bedId = "B001";

        // 模拟数据库查询
        when(bedMapper.selectById(bedId)).thenReturn(testBed);
        when(dormitoryMapper.selectById("D001")).thenReturn(testDormitory);
        when(allocationMapper.insert(any(DormitoryAllocation.class))).thenReturn(1);
        when(bedMapper.updateById(any(Bed.class))).thenReturn(1);
        when(dormitoryMapper.updateById(any(Dormitory.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> dormitoryService.chooseBed(studentId, bedId));

        // 验证调用
        verify(bedMapper).selectById(bedId);
        verify(dormitoryMapper).selectById("D001");
        verify(allocationMapper).insert(any(DormitoryAllocation.class));
        verify(bedMapper).updateById(any(Bed.class));
        verify(dormitoryMapper).updateById(any(Dormitory.class));
    }

    @Test
    void testChooseBed_BedNotFound() {
        // 准备测试数据
        String studentId = "2024001";
        String bedId = "B999";

        // 模拟床位不存在
        when(bedMapper.selectById(bedId)).thenReturn(null);

        // 执行测试并验证异常
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> dormitoryService.chooseBed(studentId, bedId));
        
        assertEquals("床位不存在", exception.getMessage());

        // 验证调用
        verify(bedMapper).selectById(bedId);
        verify(allocationMapper, never()).insert(any(DormitoryAllocation.class));
    }

    @Test
    void testChooseBed_BedOccupied() {
        // 准备测试数据
        String studentId = "2024001";
        String bedId = "B001";
        Bed occupiedBed = new Bed();
        occupiedBed.setId("B001");
        occupiedBed.setDormitoryId("D001");
        occupiedBed.setStatus("已占用");

        // 模拟床位已被占用
        when(bedMapper.selectById(bedId)).thenReturn(occupiedBed);

        // 执行测试并验证异常
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> dormitoryService.chooseBed(studentId, bedId));
        
        assertEquals("床位已被占用", exception.getMessage());

        // 验证调用
        verify(bedMapper).selectById(bedId);
        verify(allocationMapper, never()).insert(any(DormitoryAllocation.class));
    }

    @Test
    void testChooseBed_DormitoryFull() {
        // 准备测试数据
        String studentId = "2024001";
        String bedId = "B001";
        Dormitory fullDormitory = new Dormitory();
        fullDormitory.setId("D001");
        fullDormitory.setStatus("已满");

        // 模拟宿舍已满
        when(bedMapper.selectById(bedId)).thenReturn(testBed);
        when(dormitoryMapper.selectById("D001")).thenReturn(fullDormitory);

        // 执行测试并验证异常
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> dormitoryService.chooseBed(studentId, bedId));
        
        assertEquals("宿舍已满", exception.getMessage());

        // 验证调用
        verify(bedMapper).selectById(bedId);
        verify(dormitoryMapper).selectById("D001");
        verify(allocationMapper, never()).insert(any(DormitoryAllocation.class));
    }

    @Test
    void testCheckout_Success() {
        // 准备测试数据
        String studentId = "2024001";
        List<DormitoryAllocation> allocations = Arrays.asList(testAllocation);

        // 模拟数据库查询
        when(allocationMapper.selectList(any())).thenReturn(allocations);
        when(bedMapper.selectById("B001")).thenReturn(testBed);
        when(dormitoryMapper.selectById("D001")).thenReturn(testDormitory);
        when(allocationMapper.update(any(), any())).thenReturn(1);
        when(bedMapper.updateById(any(Bed.class))).thenReturn(1);
        when(dormitoryMapper.updateById(any(Dormitory.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> dormitoryService.checkout(studentId));

        // 验证调用
        verify(allocationMapper).selectList(any());
        verify(bedMapper).selectById("B001");
        verify(dormitoryMapper).selectById("D001");
    }
}

