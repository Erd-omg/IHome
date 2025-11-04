package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.common.OperationLog;
import com.ihome.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传控制器
 * 处理文件上传相关接口
 */
@RestController
@RequestMapping("/files")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 上传头像
     */
    @PostMapping("/upload/avatar")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @OperationLog(module = "文件管理", operationType = "CREATE", description = "上传头像")
    public ApiResponse<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId) {
        try {
            Map<String, Object> result = fileUploadService.uploadAvatar(file, userId);
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传维修图片
     */
    @PostMapping("/upload/repair-image")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @OperationLog(module = "文件管理", operationType = "CREATE", description = "上传维修图片")
    public ApiResponse<Map<String, Object>> uploadRepairImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("repairId") String repairId) {
        try {
            Map<String, Object> result = fileUploadService.uploadRepairImage(file, repairId);
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("维修图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传维修图片
     */
    @PostMapping("/upload/repair-images")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> uploadRepairImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("repairId") String repairId) {
        try {
            Map<String, Object> result = fileUploadService.uploadRepairImages(files, repairId);
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error("维修图片上传失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("维修图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> deleteFile(@RequestParam("filePath") String filePath) {
        try {
            Map<String, Object> result = fileUploadService.deleteFile(filePath);
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getFileInfo(@RequestParam("filePath") String filePath) {
        try {
            Map<String, Object> result = fileUploadService.getFileInfo(filePath);
            if ((Boolean) result.get("success")) {
                return ApiResponse.ok(result);
            } else {
                return ApiResponse.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return ApiResponse.error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取上传配置信息
     */
    @GetMapping("/config")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getUploadConfig() {
        try {
            Map<String, Object> config = Map.of(
                "maxFileSize", "10MB",
                "allowedTypes", "jpg,jpeg,png,gif,pdf,doc,docx",
                "avatarTypes", "jpg,jpeg,png,gif",
                "repairImageTypes", "jpg,jpeg,png,gif,pdf"
            );
            return ApiResponse.ok(config);
        } catch (Exception e) {
            return ApiResponse.error("获取配置信息失败: " + e.getMessage());
        }
    }
}

