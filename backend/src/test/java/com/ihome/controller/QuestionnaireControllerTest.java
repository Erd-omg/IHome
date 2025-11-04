package com.ihome.controller;

import com.ihome.entity.QuestionnaireAnswer;
import com.ihome.entity.RoommateTag;
import com.ihome.mapper.QuestionnaireAnswerMapper;
import com.ihome.mapper.RoommateTagMapper;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 问卷控制器测试
 */
@WebMvcTest(
    controllers = QuestionnaireController.class,
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
public class QuestionnaireControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionnaireAnswerMapper questionnaireMapper;

    @MockBean
    private RoommateTagMapper tagMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 测试前准备
    }

    @Test
    void testSubmitQuestionnaire_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> request = new HashMap<>();
        request.put("studentId", "2024001");  // 添加studentId，因为SecurityContext为空
        Map<String, String> answers = new HashMap<>();
        answers.put("sleepTimePreference", "早睡");
        answers.put("cleanlinessLevel", "爱整洁");
        answers.put("noiseTolerance", "安静");
        answers.put("eatingInRoom", "不愿意");
        answers.put("collectiveSpendingHabit", "愿意");
        request.put("answers", answers);
        request.put("tags", Arrays.asList("早睡早起", "爱干净"));

        // 模拟数据库操作
        when(questionnaireMapper.delete(any())).thenReturn(1);
        when(questionnaireMapper.insert(any(QuestionnaireAnswer.class))).thenReturn(1);
        when(tagMapper.delete(any())).thenReturn(1);
        when(tagMapper.insert(any(RoommateTag.class))).thenReturn(1);

        // 执行测试
        mockMvc.perform(post("/questionnaire/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(questionnaireMapper).delete(any());
        verify(questionnaireMapper).insert(any(QuestionnaireAnswer.class));
    }

    @Test
    void testSubmitQuestionnaire_EmptyFields() throws Exception {
        // 准备测试数据（缺少必填字段）
        Map<String, Object> request = new HashMap<>();
        Map<String, String> answers = new HashMap<>();
        answers.put("sleepTimePreference", "");
        answers.put("cleanlinessLevel", "爱整洁");
        request.put("answers", answers);

        // 执行测试
        mockMvc.perform(post("/questionnaire/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verify(questionnaireMapper, never()).insert(any(QuestionnaireAnswer.class));
    }

    @Test
    void testGetMyQuestionnaire_Success() throws Exception {
        // 准备测试数据
        QuestionnaireAnswer answer = new QuestionnaireAnswer();
        answer.setStudentId("2024001");
        answer.setSleepTimePreference("早睡");
        
        List<QuestionnaireAnswer> answers = Arrays.asList(answer);
        List<RoommateTag> tags = new ArrayList<>();

        // 模拟数据库查询
        when(questionnaireMapper.selectList(any())).thenReturn(answers);
        when(tagMapper.selectList(any())).thenReturn(tags);

        // 执行测试 - 使用实际路径 /student/{studentId}
        mockMvc.perform(get("/questionnaire/student/{studentId}", "2024001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.answers").isArray());

        verify(questionnaireMapper).selectList(any());
        verify(tagMapper).selectList(any());
    }
}

