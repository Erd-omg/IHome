package com.ihome.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 文件上传配置类
 * 配置文件上传路径和静态资源访问
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:uploads/}")
    private String uploadPath;

    @Value("${file.upload.max-size:10MB}")
    private String maxFileSize;

    @Value("${file.upload.allowed-types:jpg,jpeg,png,gif,pdf,doc,docx}")
    private String allowedTypes;

    /**
     * 配置静态资源访问路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
        
        // 配置头像访问路径
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:" + uploadPath + "avatars/");
        
        // 配置维修图片访问路径
        registry.addResourceHandler("/repair-images/**")
                .addResourceLocations("file:" + uploadPath + "repair-images/");
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public String getMaxFileSize() {
        return maxFileSize;
    }

    public String getAllowedTypes() {
        return allowedTypes;
    }
}

