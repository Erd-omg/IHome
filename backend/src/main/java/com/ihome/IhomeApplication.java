package com.ihome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.ihome.mapper") 
public class IhomeApplication {
    public static void main(String[] args) {
        SpringApplication.run(IhomeApplication.class, args);
    }
}


