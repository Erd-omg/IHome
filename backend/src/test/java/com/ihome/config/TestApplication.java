package com.ihome.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 测试专用的应用类
 * 不包含@MapperScan，避免在WebMvcTest中扫描Mapper
 * 使用ComponentScan排除mapper包，避免自动创建Mapper bean
 */
@SpringBootApplication
@ComponentScan(
    basePackages = "com.ihome",
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.ihome\\.mapper\\..*"
        )
    }
)
public class TestApplication {
}

