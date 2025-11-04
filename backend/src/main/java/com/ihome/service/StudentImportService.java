package com.ihome.service;

import com.ihome.entity.Student;
import com.ihome.mapper.StudentMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class StudentImportService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 下载导入模板
     */
    public byte[] generateTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("学生信息");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学号", "姓名", "性别", "专业", "年级", "学院", "手机号", "密码"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 创建示例数据行
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("2024001");
        exampleRow.createCell(1).setCellValue("张三");
        exampleRow.createCell(2).setCellValue("男");
        exampleRow.createCell(3).setCellValue("计算机科学");
        exampleRow.createCell(4).setCellValue("2024");
        exampleRow.createCell(5).setCellValue("计算机学院");
        exampleRow.createCell(6).setCellValue("13800138000");
        exampleRow.createCell(7).setCellValue("password");

        // 调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * 验证Excel数据
     */
    public Map<String, Object> validateFile(MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<Map<String, Object>> validData = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();

            // 跳过标题行
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> rowData = new HashMap<>();
                Map<String, String> rowErrors = new HashMap<>();

                // 读取学号
                String id = getCellValue(row, 0);
                if (id == null || id.trim().isEmpty()) {
                    rowErrors.put("学号", "学号不能为空");
                } else {
                    // 检查学号是否已存在
                    Student existingStudent = studentMapper.selectById(id);
                    if (existingStudent != null) {
                        warnings.add("第" + (i + 1) + "行：学号 " + id + " 已存在");
                    }
                    rowData.put("id", id);
                }

                // 读取姓名
                String name = getCellValue(row, 1);
                if (name == null || name.trim().isEmpty()) {
                    rowErrors.put("姓名", "姓名不能为空");
                } else {
                    rowData.put("name", name);
                }

                // 读取性别
                String gender = getCellValue(row, 2);
                if (gender == null || gender.trim().isEmpty()) {
                    gender = "男"; // 默认值
                }
                if (!gender.equals("男") && !gender.equals("女")) {
                    rowErrors.put("性别", "性别只能是'男'或'女'");
                } else {
                    rowData.put("gender", gender);
                }

                // 读取专业
                String major = getCellValue(row, 3);
                if (major == null || major.trim().isEmpty()) {
                    rowErrors.put("专业", "专业不能为空");
                } else {
                    rowData.put("major", major);
                }

                // 读取年级
                String grade = getCellValue(row, 4);
                if (grade == null || grade.trim().isEmpty()) {
                    grade = "2024"; // 默认值
                }
                rowData.put("grade", grade);

                // 读取学院
                String college = getCellValue(row, 5);
                if (college == null || college.trim().isEmpty()) {
                    rowErrors.put("学院", "学院不能为空");
                } else {
                    rowData.put("college", college);
                }

                // 读取手机号
                String phone = getCellValue(row, 6);
                if (phone != null && !phone.trim().isEmpty()) {
                    rowData.put("phoneNumber", phone);
                }

                // 读取密码
                String password = getCellValue(row, 7);
                if (password == null || password.trim().isEmpty()) {
                    password = "password"; // 默认密码
                }
                rowData.put("password", passwordEncoder.encode(password));

                // 设置默认值
                rowData.put("status", "在校");

                if (rowErrors.isEmpty()) {
                    validData.add(rowData);
                } else {
                    errors.add("第" + (i + 1) + "行：" + String.join(", ", rowErrors.values()));
                }
            }

            workbook.close();
        }

        result.put("isValid", errors.isEmpty());
        result.put("errors", errors);
        result.put("warnings", warnings);
        result.put("validData", validData);
        result.put("totalRows", validData.size() + errors.size());
        result.put("validRows", validData.size());
        result.put("errorRows", errors.size());

        return result;
    }

    /**
     * 导入学生数据
     */
    public Map<String, Object> importStudents(MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        // 先验证数据
        Map<String, Object> validationResult = validateFile(file);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> validData = (List<Map<String, Object>>) validationResult.get("validData");

        // 导入数据
        for (Map<String, Object> studentData : validData) {
            try {
                Student student = new Student();
                student.setId((String) studentData.get("id"));
                student.setName((String) studentData.get("name"));
                student.setGender((String) studentData.get("gender"));
                student.setMajor((String) studentData.get("major"));
                student.setGrade((String) studentData.get("grade"));
                student.setCollege((String) studentData.get("college"));
                student.setPhoneNumber((String) studentData.get("phoneNumber"));
                student.setPassword((String) studentData.get("password"));
                student.setStatus((String) studentData.get("status"));

                int insertResult = studentMapper.insert(student);
                if (insertResult > 0) {
                    successCount++;
                } else {
                    errorCount++;
                    errors.add("学号 " + student.getId() + " 导入失败");
                }
            } catch (Exception e) {
                errorCount++;
                errors.add("学号 " + studentData.get("id") + " 导入失败：" + e.getMessage());
            }
        }

        result.put("totalRows", validData.size());
        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("warningCount", warnings.size());
        result.put("errors", errors);
        result.put("warnings", warnings);

        return result;
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 防止科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}

