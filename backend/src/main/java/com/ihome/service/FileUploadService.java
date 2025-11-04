package com.ihome.service;

import com.ihome.config.FileUploadConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件上传服务类
 * 处理文件上传、验证、存储等功能
 */
@Service
public class FileUploadService {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    /**
     * 上传头像文件
     */
    public Map<String, Object> uploadAvatar(MultipartFile file, String userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证文件
            Map<String, Object> validation = validateFile(file, "avatar");
            if (!(Boolean) validation.get("valid")) {
                return validation;
            }
            
            // 创建头像目录
            String avatarDir = fileUploadConfig.getUploadPath() + "avatars/";
            createDirectoryIfNotExists(avatarDir);
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = userId + "_" + System.currentTimeMillis() + "." + extension;
            
            // 保存文件
            Path filePath = Paths.get(avatarDir + filename);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回结果
            result.put("success", true);
            result.put("message", "头像上传成功");
            result.put("filename", filename);
            result.put("url", "/avatars/" + filename);
            result.put("size", file.getSize());
            result.put("uploadTime", LocalDateTime.now());
            
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "头像上传失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 上传维修图片
     */
    public Map<String, Object> uploadRepairImage(MultipartFile file, String repairId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证文件
            Map<String, Object> validation = validateFile(file, "repair");
            if (!(Boolean) validation.get("valid")) {
                return validation;
            }
            
            // 创建维修图片目录
            String repairDir = fileUploadConfig.getUploadPath() + "repair-images/";
            createDirectoryIfNotExists(repairDir);
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = repairId + "_" + System.currentTimeMillis() + "." + extension;
            
            // 保存文件
            Path filePath = Paths.get(repairDir + filename);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回结果
            result.put("success", true);
            result.put("message", "维修图片上传成功");
            result.put("filename", filename);
            result.put("url", "/repair-images/" + filename);
            result.put("size", file.getSize());
            result.put("uploadTime", LocalDateTime.now());
            
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "维修图片上传失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 批量上传维修图片
     */
    public Map<String, Object> uploadRepairImages(MultipartFile[] files, String repairId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> uploadedFiles = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                Map<String, Object> uploadResult = uploadRepairImage(file, repairId);
                if ((Boolean) uploadResult.get("success")) {
                    uploadedFiles.add(uploadResult);
                } else {
                    errors.add((String) uploadResult.get("message"));
                }
            }
        }
        
        result.put("success", uploadedFiles.size() > 0);
        result.put("uploadedFiles", uploadedFiles);
        result.put("errors", errors);
        result.put("totalFiles", files.length);
        result.put("successCount", uploadedFiles.size());
        result.put("errorCount", errors.size());
        
        return result;
    }

    /**
     * 删除文件
     */
    public Map<String, Object> deleteFile(String filePath) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Path path = Paths.get(fileUploadConfig.getUploadPath() + filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                result.put("success", true);
                result.put("message", "文件删除成功");
            } else {
                result.put("success", false);
                result.put("message", "文件不存在");
            }
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "文件删除失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取文件信息
     */
    public Map<String, Object> getFileInfo(String filePath) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Path path = Paths.get(fileUploadConfig.getUploadPath() + filePath);
            if (Files.exists(path)) {
                result.put("success", true);
                result.put("filename", path.getFileName().toString());
                result.put("size", Files.size(path));
                result.put("lastModified", Files.getLastModifiedTime(path));
                result.put("exists", true);
            } else {
                result.put("success", false);
                result.put("message", "文件不存在");
                result.put("exists", false);
            }
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "获取文件信息失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 验证文件
     */
    private Map<String, Object> validateFile(MultipartFile file, String type) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查文件是否为空
        if (file.isEmpty()) {
            result.put("valid", false);
            result.put("message", "文件不能为空");
            return result;
        }
        
        // 检查文件大小
        long maxSize = parseFileSize(fileUploadConfig.getMaxFileSize());
        if (file.getSize() > maxSize) {
            result.put("valid", false);
            result.put("message", "文件大小超过限制，最大允许 " + fileUploadConfig.getMaxFileSize());
            return result;
        }
        
        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            result.put("valid", false);
            result.put("message", "文件名不能为空");
            return result;
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        String[] allowedTypes = fileUploadConfig.getAllowedTypes().split(",");
        
        boolean typeAllowed = false;
        for (String allowedType : allowedTypes) {
            if (allowedType.trim().equals(extension)) {
                typeAllowed = true;
                break;
            }
        }
        
        if (!typeAllowed) {
            result.put("valid", false);
            result.put("message", "不支持的文件类型，允许的类型: " + fileUploadConfig.getAllowedTypes());
            return result;
        }
        
        // 根据类型进行额外验证
        if ("avatar".equals(type)) {
            if (!Arrays.asList("jpg", "jpeg", "png", "gif").contains(extension)) {
                result.put("valid", false);
                result.put("message", "头像只支持 jpg, jpeg, png, gif 格式");
                return result;
            }
        }
        
        result.put("valid", true);
        return result;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    /**
     * 解析文件大小字符串
     */
    private long parseFileSize(String sizeStr) {
        if (sizeStr == null || sizeStr.trim().isEmpty()) {
            return 10 * 1024 * 1024; // 默认10MB
        }
        
        sizeStr = sizeStr.trim().toUpperCase();
        long multiplier = 1;
        
        if (sizeStr.endsWith("KB")) {
            multiplier = 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("MB")) {
            multiplier = 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        }
        
        try {
            return Long.parseLong(sizeStr) * multiplier;
        } catch (NumberFormatException e) {
            return 10 * 1024 * 1024; // 默认10MB
        }
    }

    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
}

