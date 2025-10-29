package com.ihome.service;

import com.ihome.entity.*;
import com.ihome.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 智能分配算法服务测试
 */
@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
public class AllocationServiceTest {

    @Mock
    private StudentMapper studentMapper;
    
    @Mock
    private DormitoryMapper dormitoryMapper;
    
    @Mock
    private BedMapper bedMapper;
    
    @Mock
    private DormitoryAllocationMapper allocationMapper;
    
    @Mock
    private QuestionnaireAnswerMapper questionnaireMapper;
    
    @Mock
    private RoommateTagMapper tagMapper;
    
    @Mock
    private AllocationFeedbackMapper feedbackMapper;
    
    @Mock
    private AlgorithmWeightsMapper weightsMapper;

    @InjectMocks
    private AllocationService allocationService;

    private List<Student> testStudents;
    private List<Bed> testBeds;
    private List<QuestionnaireAnswer> testQuestionnaires;
    private List<RoommateTag> testTags;
    private List<AlgorithmWeights> testWeights;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        setupTestData();
    }

    private void setupTestData() {
        // 创建测试学生
        testStudents = Arrays.asList(
            createStudent("S001", "张三", "男", "计算机科学", "2023"),
            createStudent("S002", "李四", "男", "计算机科学", "2023"),
            createStudent("S003", "王五", "女", "软件工程", "2023"),
            createStudent("S004", "赵六", "女", "软件工程", "2023")
        );

        // 创建测试床位
        testBeds = Arrays.asList(
            createBed("B001", "D001", "下铺", "可用"),
            createBed("B002", "D001", "上铺", "可用"),
            createBed("B003", "D002", "下铺", "可用"),
            createBed("B004", "D002", "上铺", "可用")
        );

        // 创建测试问卷答案
        testQuestionnaires = Arrays.asList(
            createQuestionnaire("S001", "早睡", "爱整洁", "安静", "愿意"),
            createQuestionnaire("S002", "晚睡", "一般", "能接受一点噪音", "一般"),
            createQuestionnaire("S003", "早睡", "爱整洁", "安静", "愿意"),
            createQuestionnaire("S004", "晚睡", "一般", "能接受一点噪音", "一般")
        );

        // 创建测试标签
        testTags = Arrays.asList(
            createTag("S001", "安静"),
            createTag("S001", "爱学习"),
            createTag("S002", "友善"),
            createTag("S003", "整洁"),
            createTag("S004", "负责任")
        );

        // 创建测试权重配置
        testWeights = Arrays.asList(
            createWeight("QUESTIONNAIRE", 0.5, true),
            createWeight("TAG", 0.4, true),
            createWeight("MAJOR", 0.3, true),
            createWeight("GENDER", 0.2, true),
            createWeight("BED_TYPE", 0.1, true)
        );
    }

    private Student createStudent(String id, String name, String gender, String major, String grade) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setGender(gender);
        student.setMajor(major);
        student.setGrade(grade);
        student.setStatus("在校");
        return student;
    }

    private Bed createBed(String id, String dormitoryId, String bedType, String status) {
        Bed bed = new Bed();
        bed.setId(id);
        bed.setDormitoryId(dormitoryId);
        bed.setBedType(bedType);
        bed.setStatus(status);
        return bed;
    }

    private QuestionnaireAnswer createQuestionnaire(String studentId, String sleepTime, String cleanliness, String noise, String spending) {
        QuestionnaireAnswer questionnaire = new QuestionnaireAnswer();
        questionnaire.setStudentId(studentId);
        questionnaire.setSleepTimePreference(sleepTime);
        questionnaire.setCleanlinessLevel(cleanliness);
        questionnaire.setNoiseTolerance(noise);
        questionnaire.setCollectiveSpendingHabit(spending);
        return questionnaire;
    }

    private RoommateTag createTag(String studentId, String tagName) {
        RoommateTag tag = new RoommateTag();
        tag.setStudentId(studentId);
        tag.setTagName(tagName);
        return tag;
    }

    private AlgorithmWeights createWeight(String weightType, Double weightValue, Boolean enabled) {
        AlgorithmWeights weight = new AlgorithmWeights();
        weight.setWeightType(weightType);
        weight.setWeightValue(weightValue);
        weight.setEnabled(enabled);
        weight.setDescription("测试权重配置");
        weight.setCreatedAt(LocalDateTime.now());
        weight.setLastUpdated(LocalDateTime.now());
        return weight;
    }

    @Test
    void testIntelligentAllocation_Success() {
        // 准备测试数据
        List<String> studentIds = Arrays.asList("S001", "S002", "S003", "S004");
        
        // 模拟数据库查询
        when(studentMapper.selectById("S001")).thenReturn(testStudents.get(0));
        when(studentMapper.selectById("S002")).thenReturn(testStudents.get(1));
        when(studentMapper.selectById("S003")).thenReturn(testStudents.get(2));
        when(studentMapper.selectById("S004")).thenReturn(testStudents.get(3));
        
        when(bedMapper.selectList(null)).thenReturn(testBeds);
        when(questionnaireMapper.selectByStudentId(anyString())).thenReturn(testQuestionnaires.get(0));
        when(tagMapper.selectByStudentId(anyString())).thenReturn(testTags);
        when(weightsMapper.selectList(null)).thenReturn(testWeights);
        when(allocationMapper.selectByDormitoryId(anyString())).thenReturn(new ArrayList<>());
        
        // 执行测试
        Map<String, Object> result = allocationService.intelligentAllocation(studentIds);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("allocations"));
        assertTrue(result.containsKey("unallocatedStudents"));
        assertTrue(result.containsKey("totalAllocated"));
        assertTrue(result.containsKey("totalUnallocated"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allocations = (List<Map<String, Object>>) result.get("allocations");
        assertNotNull(allocations);
        
        // 验证分配结果
        for (Map<String, Object> allocation : allocations) {
            assertTrue(allocation.containsKey("studentId"));
            assertTrue(allocation.containsKey("bedId"));
            assertTrue(allocation.containsKey("dormitoryId"));
            assertTrue(allocation.containsKey("gender"));
        }
        
        // 验证数据库操作被调用
        verify(allocationMapper, atLeastOnce()).insert(any(DormitoryAllocation.class));
        verify(bedMapper, atLeastOnce()).updateById(any(Bed.class));
    }

    @Test
    void testGetAllocationSuggestions_Success() {
        // 准备测试数据
        String studentId = "S001";
        Student targetStudent = testStudents.get(0);
        
        // 模拟数据库查询
        when(studentMapper.selectById(studentId)).thenReturn(targetStudent);
        when(studentMapper.selectByMajor(targetStudent.getMajor())).thenReturn(testStudents);
        when(questionnaireMapper.selectByStudentId(anyString())).thenReturn(testQuestionnaires.get(0));
        when(tagMapper.selectByStudentId(anyString())).thenReturn(testTags);
        when(weightsMapper.selectList(null)).thenReturn(testWeights);
        
        // 执行测试
        Map<String, Object> result = allocationService.getAllocationSuggestions(studentId);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.containsKey("error"));
        assertTrue(result.containsKey("suggestions"));
        assertTrue(result.containsKey("studentMajor"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        assertNotNull(suggestions);
        assertTrue(suggestions.size() <= 5); // 最多返回5个建议
        
        // 验证建议按兼容性分数排序
        for (int i = 0; i < suggestions.size() - 1; i++) {
            double score1 = (Double) suggestions.get(i).get("compatibilityScore");
            double score2 = (Double) suggestions.get(i + 1).get("compatibilityScore");
            assertTrue(score1 >= score2, "建议应该按兼容性分数降序排列");
        }
    }

    @Test
    void testSubmitAllocationFeedback_Success() {
        // 准备测试数据
        AllocationFeedback feedback = new AllocationFeedback();
        feedback.setStudentId("S001");
        feedback.setAllocationId(1L);
        feedback.setRoommateSatisfaction(4);
        feedback.setEnvironmentSatisfaction(5);
        feedback.setOverallSatisfaction(4);
        feedback.setFeedbackContent("分配结果很满意");
        
        // 模拟数据库操作
        when(weightsMapper.selectList(null)).thenReturn(testWeights);
        when(weightsMapper.updateById(any(AlgorithmWeights.class))).thenReturn(1);
        
        // 执行测试
        Map<String, Object> result = allocationService.submitAllocationFeedback(feedback);
        
        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("反馈提交成功", result.get("message"));
        assertNotNull(result.get("feedbackId"));
        
        // 验证数据库操作被调用
        verify(feedbackMapper).insert(feedback);
    }

    @Test
    void testGetAllocationStatistics_Success() {
        // 准备测试数据
        List<AllocationFeedback> feedbacks = Arrays.asList(
            createFeedback("S001", 4, 5, 4, "满意"),
            createFeedback("S002", 3, 3, 3, "一般"),
            createFeedback("S003", 5, 5, 5, "非常满意")
        );
        
        // 模拟数据库查询
        when(feedbackMapper.selectList(null)).thenReturn(feedbacks);
        when(weightsMapper.selectList(null)).thenReturn(testWeights);
        
        // 执行测试
        Map<String, Object> result = allocationService.getAllocationStatistics();
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("totalFeedbacks"));
        assertTrue(result.containsKey("averageRoommateSatisfaction"));
        assertTrue(result.containsKey("averageEnvironmentSatisfaction"));
        assertTrue(result.containsKey("averageOverallSatisfaction"));
        assertTrue(result.containsKey("satisfactionDistribution"));
        assertTrue(result.containsKey("currentWeights"));
        
        assertEquals(3, result.get("totalFeedbacks"));
        
        // 验证平均满意度计算
        double avgRoommate = (Double) result.get("averageRoommateSatisfaction");
        double avgEnvironment = (Double) result.get("averageEnvironmentSatisfaction");
        double avgOverall = (Double) result.get("averageOverallSatisfaction");
        
        assertEquals(4.0, avgRoommate, 0.1);
        assertEquals(4.33, avgEnvironment, 0.1);
        assertEquals(4.0, avgOverall, 0.1);
    }

    @Test
    void testGetStudentFeedback_Success() {
        // 准备测试数据
        String studentId = "S001";
        List<AllocationFeedback> studentFeedbacks = Arrays.asList(
            createFeedback(studentId, 4, 5, 4, "满意"),
            createFeedback(studentId, 3, 3, 3, "一般")
        );
        
        // 模拟数据库查询
        when(feedbackMapper.selectList(null)).thenReturn(studentFeedbacks);
        
        // 执行测试
        List<AllocationFeedback> result = allocationService.getStudentFeedback(studentId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(f -> f.getStudentId().equals(studentId)));
    }

    @Test
    void testIntelligentAllocation_NoAvailableBeds() {
        // 准备测试数据 - 没有可用床位
        List<String> studentIds = Arrays.asList("S001", "S002");
        List<Bed> noAvailableBeds = testBeds.stream()
                .map(bed -> {
                    Bed occupiedBed = new Bed();
                    occupiedBed.setId(bed.getId());
                    occupiedBed.setDormitoryId(bed.getDormitoryId());
                    occupiedBed.setBedType(bed.getBedType());
                    occupiedBed.setStatus("已占用");
                    return occupiedBed;
                })
                .collect(java.util.stream.Collectors.toList());
        
        // 模拟数据库查询
        when(studentMapper.selectById("S001")).thenReturn(testStudents.get(0));
        when(studentMapper.selectById("S002")).thenReturn(testStudents.get(1));
        when(bedMapper.selectList(null)).thenReturn(noAvailableBeds);
        
        // 执行测试
        Map<String, Object> result = allocationService.intelligentAllocation(studentIds);
        
        // 验证结果
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allocations = (List<Map<String, Object>>) result.get("allocations");
        @SuppressWarnings("unchecked")
        List<String> unallocatedStudents = (List<String>) result.get("unallocatedStudents");
        
        assertTrue(allocations.isEmpty());
        assertEquals(2, unallocatedStudents.size());
        assertTrue(unallocatedStudents.contains("S001"));
        assertTrue(unallocatedStudents.contains("S002"));
    }

    @Test
    void testGetAllocationSuggestions_StudentNotFound() {
        // 准备测试数据 - 学生不存在
        String nonExistentStudentId = "NONEXISTENT";
        
        // 模拟数据库查询
        when(studentMapper.selectById(nonExistentStudentId)).thenReturn(null);
        
        // 执行测试
        Map<String, Object> result = allocationService.getAllocationSuggestions(nonExistentStudentId);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("error"));
        assertEquals("学生不存在", result.get("error"));
    }

    private AllocationFeedback createFeedback(String studentId, int roommate, int environment, int overall, String comments) {
        AllocationFeedback feedback = new AllocationFeedback();
        feedback.setStudentId(studentId);
        feedback.setAllocationId(1L);
        feedback.setRoommateSatisfaction(roommate);
        feedback.setEnvironmentSatisfaction(environment);
        feedback.setOverallSatisfaction(overall);
        feedback.setFeedbackContent(comments);
        feedback.setFeedbackTime(LocalDateTime.now());
        feedback.setCreatedAt(LocalDateTime.now());
        return feedback;
    }
}
