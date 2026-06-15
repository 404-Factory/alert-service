package com.factory.alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// inbox-jpa 가 자체 @EnableJpaRepositories 를 선언하면 Spring Boot 기본 레포지토리 스캔이
// 비활성화되므로, alert-service 의 엔티티/레포지토리 패키지를 명시적으로 스캔하도록 선언한다.
@SpringBootApplication
@EntityScan(basePackages = "com.factory.alert")
@EnableJpaRepositories(basePackages = "com.factory.alert")
public class AlertServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertServiceApplication.class, args);
    }
}
