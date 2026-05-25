package com.street.street.patrol;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.street.street.patrol.mapper")
public class PatrolApplication {
    public static void main(String[] args) {
        SpringApplication.run(PatrolApplication.class, args);
    }
}
