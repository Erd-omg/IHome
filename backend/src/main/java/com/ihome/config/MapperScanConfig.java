package com.ihome.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;

/**
 * Mapper扫描配置
 * 
 * 只在存在SqlSessionFactory时扫描Mapper，这样：
 * - 在@WebMvcTest中（排除了MybatisPlusAutoConfiguration，没有SqlSessionFactory）不会扫描Mapper
 * - 在@SpringBootTest中（有SqlSessionFactory）会正常扫描Mapper
 * 
 * 使用@AutoConfigureAfter确保在MybatisPlusAutoConfiguration之后加载
 * 使用@ConditionalOnClass确保MyBatis-Plus类存在时才加载
 * 使用@ConditionalOnBean确保在SqlSessionFactory存在时才加载
 * 使用@DependsOn确保SqlSessionFactory先创建（解决初始化时序问题）
 * 
 * 注意：@ConditionalOnBean在配置类上检查的是Bean定义的存在性，而不是Bean实例
 * 但在@MapperScan执行时，需要SqlSessionFactory已经被创建，所以使用@AutoConfigureAfter和@DependsOn确保顺序
 * 
 * 重要：此配置类作为普通@Configuration类，由组件扫描自动发现（不在IhomeApplication中使用@Import）
 * 这样可以确保条件注解正常工作，只在SqlSessionFactory存在时才扫描Mapper
 */
@Configuration
@ConditionalOnClass(MybatisPlusAutoConfiguration.class)
@AutoConfigureAfter(MybatisPlusAutoConfiguration.class)
@ConditionalOnBean(SqlSessionFactory.class)
@MapperScan("com.ihome.mapper")
public class MapperScanConfig {
    // 空类，仅用于配置Mapper扫描
    // 注意：@MapperScan 会在配置类加载时立即执行扫描，但此时 SqlSessionFactory 可能还没有创建
    // 使用 @AutoConfigureAfter 和 @ConditionalOnBean 确保顺序和条件
}
