package com.ihome.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

/**
 * 测试环境 Mapper 扫描配置
 * 注意：此配置类已不再使用，因为 MapperScanConfig 已经由组件扫描自动发现
 * 保留此类是为了避免破坏其他可能依赖它的代码
 * 
 * 在集成测试中，如果需要排除 AdminMapper，应该：
 * 1. 使用 @MockBean 提供 Mock 的 AdminMapper
 * 2. 或者排除 AdminController（如果测试不需要它）
 */
@TestConfiguration
@Profile("test")
@MapperScan("com.ihome.mapper")
public class TestMapperScanConfig {
    // 注意：此配置类实际上不应该被使用，因为会导致 Mapper 在 SqlSessionFactory 之前被扫描
    // 真正的 Mapper 扫描应该通过 MapperScanConfig（由组件扫描自动发现）来完成
}

