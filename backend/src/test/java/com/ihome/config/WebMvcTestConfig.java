package com.ihome.config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;

/**
 * WebMvcTest 通用配置
 * 用于排除不必要的自动配置
 */
@TestConfiguration
@ImportAutoConfiguration(exclude = {
    SecurityAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class,
    DataSourceAutoConfiguration.class,
    SqlInitializationAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    MybatisPlusAutoConfiguration.class
})
public class WebMvcTestConfig {
}

