package com.ihome.controller;

import com.ihome.entity.RoommateTag;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 生活方式标签控制器测试
 */
@WebMvcTest(
    controllers = LifestyleTagController.class,
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
public class LifestyleTagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoommateTagMapper roommateTagMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private RoommateTag testTag;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testTag = new RoommateTag();
        testTag.setId(1);
        testTag.setStudentId("2024001");
        testTag.setTagName("早睡早起");
    }

    @Test
    void testGetAvailableTags_Success() throws Exception {
        mockMvc.perform(get("/lifestyle-tags/available")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(15));
    }

    @Test
    void testGetMyTags_Success() throws Exception {
        // 模拟数据库查询
        when(roommateTagMapper.selectList(any())).thenReturn(Arrays.asList(testTag));

        mockMvc.perform(get("/lifestyle-tags/my-tags")
                        .param("studentId", "2024001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(roommateTagMapper).selectList(any());
    }

    @Test
    void testSetStudentTags_Success() throws Exception {
        // 准备测试数据
        List<String> tagNames = Arrays.asList("早睡早起", "爱干净");

        // 模拟数据库操作
        when(roommateTagMapper.delete(any())).thenReturn(1);
        when(roommateTagMapper.insert(any(RoommateTag.class))).thenReturn(1);

        mockMvc.perform(post("/lifestyle-tags/set")
                        .param("studentId", "2024001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagNames))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(roommateTagMapper).delete(any());
        verify(roommateTagMapper, times(2)).insert(any(RoommateTag.class));
    }

    @Test
    void testSetStudentTags_EmptyList() throws Exception {
        // 准备测试数据
        List<String> tagNames = Arrays.asList();

        // 模拟数据库操作
        when(roommateTagMapper.delete(any())).thenReturn(1);

        mockMvc.perform(post("/lifestyle-tags/set")
                        .param("studentId", "2024001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagNames))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(roommateTagMapper).delete(any());
        verify(roommateTagMapper, never()).insert(any(RoommateTag.class));
    }
}

