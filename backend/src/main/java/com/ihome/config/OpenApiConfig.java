package com.ihome.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI配置类
 * 提供API文档的完整配置信息
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IHome宿舍管理系统API文档")
                        .version("v1.0")
                        .description("IHome宿舍管理系统RESTful API接口文档\n\n" +
                                "系统功能模块：\n" +
                                "- 用户认证与授权\n" +
                                "- 宿舍管理\n" +
                                "- 缴费管理\n" +
                                "- 维修管理\n" +
                                "- 通知公告\n" +
                                "- 调换申请\n" +
                                "- 智能分配\n" +
                                "- 数据统计\n" +
                                "- 学生批量导入\n" +
                                "- 床位自主选择\n" +
                                "- 调换推荐\n" +
                                "- 维修反馈\n" +
                                "- 生活习惯标签\n\n" +
                                "**认证方式**：JWT Bearer Token\n" +
                                "**Token有效期**：2小时\n" +
                                "**Refresh Token有效期**：7天")
                        .contact(new Contact()
                                .name("IHome开发团队")
                                .email("support@ihome.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入JWT Token，格式为：Bearer {token}")));
    }
}

