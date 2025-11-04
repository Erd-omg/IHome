package com.ihome.service;

import com.ihome.entity.*;
import com.ihome.mapper.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 数据导出服务
 */
@Service
public class DataExportService {
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private DormitoryMapper dormitoryMapper;
    
    @Autowired
    private PaymentRecordMapper paymentMapper;
    
    @Autowired
    private RepairOrderMapper repairMapper;
    
    @Autowired
    private DormitoryAllocationMapper allocationMapper;
    
    /**
     * 导出学生信息到Excel
     */
    public byte[] exportStudentsToExcel() throws IOException {
        List<Student> students = studentMapper.selectList(null);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("学生信息");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学号", "姓名", "性别", "专业", "年级", "邮箱", "状态"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        int rowNum = 1;
        for (Student student : students) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getId());
            row.createCell(1).setCellValue(student.getName());
            row.createCell(2).setCellValue(student.getGender());
            row.createCell(3).setCellValue(student.getMajor());
            row.createCell(4).setCellValue(student.getGrade());
            row.createCell(5).setCellValue(student.getEmail());
            row.createCell(6).setCellValue(student.getStatus());
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * 导出宿舍信息到Excel
     */
    public byte[] exportDormitoriesToExcel() throws IOException {
        List<Dormitory> dormitories = dormitoryMapper.selectList(null);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("宿舍信息");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"宿舍ID", "建筑ID", "楼层", "房间号", "房间类型", "床位数", "当前入住", "状态"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        int rowNum = 1;
        for (Dormitory dormitory : dormitories) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dormitory.getId());
            row.createCell(1).setCellValue(dormitory.getBuildingId());
            row.createCell(2).setCellValue(dormitory.getFloorNumber());
            row.createCell(3).setCellValue(dormitory.getRoomNumber());
            row.createCell(4).setCellValue(dormitory.getRoomType());
            row.createCell(5).setCellValue(dormitory.getBedCount());
            row.createCell(6).setCellValue(dormitory.getCurrentOccupancy());
            row.createCell(7).setCellValue(dormitory.getStatus());
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * 导出缴费记录到Excel
     */
    public byte[] exportPaymentsToExcel() throws IOException {
        List<PaymentRecord> payments = paymentMapper.selectList(null);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("缴费记录");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"记录ID", "学生ID", "金额", "缴费方式", "缴费时间"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        int rowNum = 1;
        for (PaymentRecord payment : payments) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(payment.getId());
            row.createCell(1).setCellValue(payment.getStudentId());
            row.createCell(2).setCellValue(payment.getAmount().doubleValue());
            row.createCell(3).setCellValue(payment.getPaymentMethod());
            row.createCell(4).setCellValue(payment.getPaymentTime() != null ? 
                payment.getPaymentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * 导出维修记录到Excel
     */
    public byte[] exportRepairsToExcel() throws IOException {
        List<RepairOrder> repairs = repairMapper.selectList(null);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("维修记录");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"工单ID", "学生ID", "宿舍ID", "维修类型", "描述", "紧急程度", "状态", "创建时间", "更新时间"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        int rowNum = 1;
        for (RepairOrder repair : repairs) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(repair.getId());
            row.createCell(1).setCellValue(repair.getStudentId());
            row.createCell(2).setCellValue(repair.getDormitoryId());
            row.createCell(3).setCellValue(repair.getRepairType());
            row.createCell(4).setCellValue(repair.getDescription());
            row.createCell(5).setCellValue(repair.getUrgencyLevel());
            row.createCell(6).setCellValue(repair.getStatus());
            row.createCell(7).setCellValue(repair.getCreatedAt() != null ? 
                repair.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            row.createCell(8).setCellValue(repair.getUpdatedAt() != null ? 
                repair.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * 导出分配记录到Excel
     */
    public byte[] exportAllocationsToExcel() throws IOException {
        List<DormitoryAllocation> allocations = allocationMapper.selectList(null);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("分配记录");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"分配ID", "学生ID", "床位ID", "入住日期", "退宿日期", "状态"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        int rowNum = 1;
        for (DormitoryAllocation allocation : allocations) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(allocation.getId());
            row.createCell(1).setCellValue(allocation.getStudentId());
            row.createCell(2).setCellValue(allocation.getBedId());
            row.createCell(3).setCellValue(allocation.getCheckInDate() != null ? 
                allocation.getCheckInDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
            row.createCell(4).setCellValue(allocation.getCheckOutDate() != null ? 
                allocation.getCheckOutDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
            row.createCell(5).setCellValue(allocation.getStatus());
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
}
