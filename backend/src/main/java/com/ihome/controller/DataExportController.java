package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.common.OperationLog;
import com.ihome.service.DataExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 数据导出控制器
 */
@RestController
@RequestMapping("/export")
public class DataExportController {
    
    @Autowired
    private DataExportService dataExportService;
    
    /**
     * 导出学生信息到Excel
     */
    @GetMapping("/students/excel")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "数据导出", operationType = "QUERY", description = "导出学生信息到Excel")
    public ResponseEntity<byte[]> exportStudentsToExcel() throws IOException {
        byte[] data = dataExportService.exportStudentsToExcel();
        
        String filename = "学生信息_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
    
    /**
     * 导出宿舍信息到Excel
     */
    @GetMapping("/dormitories/excel")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "数据导出", operationType = "QUERY", description = "导出宿舍信息到Excel")
    public ResponseEntity<byte[]> exportDormitoriesToExcel() throws IOException {
        byte[] data = dataExportService.exportDormitoriesToExcel();
        
        String filename = "宿舍信息_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
    
    /**
     * 导出缴费记录到Excel
     */
    @GetMapping("/payments/excel")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "数据导出", operationType = "QUERY", description = "导出缴费记录到Excel")
    public ResponseEntity<byte[]> exportPaymentsToExcel() throws IOException {
        byte[] data = dataExportService.exportPaymentsToExcel();
        
        String filename = "缴费记录_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
    
    /**
     * 导出维修记录到Excel
     */
    @GetMapping("/repairs/excel")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "数据导出", operationType = "QUERY", description = "导出维修记录到Excel")
    public ResponseEntity<byte[]> exportRepairsToExcel() throws IOException {
        byte[] data = dataExportService.exportRepairsToExcel();
        
        String filename = "维修记录_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
    
    /**
     * 导出分配记录到Excel
     */
    @GetMapping("/allocations/excel")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "数据导出", operationType = "QUERY", description = "导出分配记录到Excel")
    public ResponseEntity<byte[]> exportAllocationsToExcel() throws IOException {
        byte[] data = dataExportService.exportAllocationsToExcel();
        
        String filename = "分配记录_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
    
    /**
     * 获取导出配置信息
     */
    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getExportConfig() {
        Map<String, Object> config = Map.of(
            "supportedFormats", "xlsx",
            "maxRecords", 10000,
            "availableExports", Map.of(
                "students", "学生信息",
                "dormitories", "宿舍信息", 
                "payments", "缴费记录",
                "repairs", "维修记录",
                "allocations", "分配记录"
            )
        );
        return ApiResponse.ok(config);
    }
}
