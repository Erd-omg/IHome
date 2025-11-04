package com.ihome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * IHome 宿舍管理系统主应用类
 *
 * 注意：MapperScanConfig 作为普通 @Configuration 类，由组件扫描自动发现
 * 使用 @ConditionalOnBean(SqlSessionFactory.class) 确保只在有 SqlSessionFactory 时扫描 Mapper
 * 这样在 @WebMvcTest 中（排除了 MybatisPlusAutoConfiguration，没有 SqlSessionFactory）不会扫描 Mapper
 * 在 @SpringBootTest 中（有 SqlSessionFactory）会正常扫描 Mapper
 */
@SpringBootApplication
public class IhomeApplication {
    public static void main(String[] args) {
        SpringApplication.run(IhomeApplication.class, args);
    }
}


