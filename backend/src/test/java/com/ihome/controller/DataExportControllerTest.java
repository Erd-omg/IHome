package com.ihome.controller;

import com.ihome.service.DataExportService;
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

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 数据导出控制器测试
 */
@WebMvcTest(
    controllers = DataExportController.class,
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
public class DataExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataExportService dataExportService;

    @Test
    void testExportStudentsToExcel_Success() throws Exception {
        // 准备测试数据
        byte[] excelData = new byte[]{1, 2, 3, 4, 5};

        // 模拟服务调用
        when(dataExportService.exportStudentsToExcel()).thenReturn(excelData);

        // 执行测试
        mockMvc.perform(get("/export/students/excel")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));

        verify(dataExportService).exportStudentsToExcel();
    }

    @Test
    void testExportDormitoriesToExcel_Success() throws Exception {
        // 准备测试数据
        byte[] excelData = new byte[]{1, 2, 3, 4, 5};

        // 模拟服务调用
        when(dataExportService.exportDormitoriesToExcel()).thenReturn(excelData);

        // 执行测试
        mockMvc.perform(get("/export/dormitories/excel")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));

        verify(dataExportService).exportDormitoriesToExcel();
    }

    @Test
    void testExportPaymentsToExcel_Success() throws Exception {
        // 准备测试数据
        byte[] excelData = new byte[]{1, 2, 3, 4, 5};

        // 模拟服务调用
        when(dataExportService.exportPaymentsToExcel()).thenReturn(excelData);

        // 执行测试
        mockMvc.perform(get("/export/payments/excel")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));

        verify(dataExportService).exportPaymentsToExcel();
    }

    @Test
    void testExportRepairsToExcel_Success() throws Exception {
        // 准备测试数据
        byte[] excelData = new byte[]{1, 2, 3, 4, 5};

        // 模拟服务调用
        when(dataExportService.exportRepairsToExcel()).thenReturn(excelData);

        // 执行测试
        mockMvc.perform(get("/export/repairs/excel")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));

        verify(dataExportService).exportRepairsToExcel();
    }

    @Test
    void testExportStudentsToExcel_IOException() throws Exception {
        // 模拟服务抛出异常
        when(dataExportService.exportStudentsToExcel()).thenThrow(new IOException("导出失败"));

        // 执行测试
        mockMvc.perform(get("/export/students/excel")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().is5xxServerError());

        verify(dataExportService).exportStudentsToExcel();
    }
}

