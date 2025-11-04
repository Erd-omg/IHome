package com.ihome.controller;

import com.ihome.service.FileUploadService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 文件上传控制器测试
 */
@WebMvcTest(
    controllers = FileUploadController.class,
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
public class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileUploadService fileUploadService;

    @Test
    void testUploadAvatar_Success() throws Exception {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());
        
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "头像上传成功");
        serviceResult.put("url", "/avatars/avatar.jpg");

        // 模拟服务调用
        when(fileUploadService.uploadAvatar(any(), anyString())).thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(multipart("/files/upload/avatar")
                        .file(file)
                        .param("userId", "2024001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true));

        verify(fileUploadService).uploadAvatar(any(), eq("2024001"));
    }

    @Test
    void testUploadRepairImage_Success() throws Exception {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
                "file", "repair.jpg", MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());
        
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "维修图片上传成功");
        serviceResult.put("url", "/repair-images/repair.jpg");

        // 模拟服务调用
        when(fileUploadService.uploadRepairImage(any(), anyString())).thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(multipart("/files/upload/repair-image")
                        .file(file)
                        .param("repairId", "1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(fileUploadService).uploadRepairImage(any(), eq("1"));
    }

    @Test
    void testUploadRepairImages_Success() throws Exception {
        // 准备测试数据
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "repair1.jpg", MediaType.IMAGE_JPEG_VALUE, "test image1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "repair2.jpg", MediaType.IMAGE_JPEG_VALUE, "test image2".getBytes());
        
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("uploadedCount", 2);

        // 模拟服务调用
        when(fileUploadService.uploadRepairImages(any(), anyString())).thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(multipart("/files/upload/repair-images")
                        .file(file1)
                        .file(file2)
                        .param("repairId", "1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(fileUploadService).uploadRepairImages(any(), eq("1"));
    }
}

