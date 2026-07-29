package com.marry;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * marry-platform main entrypoint.
 *
 * <p>Package layout: {@code com.marry} (with sub-packages {@code web, config, ...}).
 * Other modules use their own root packages ({@code com.marry.system}, {@code com.marry.security}, ...).
 * The mapper scan covers all persistence mappers.</p>
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "com.marry",
        "com.marry.common",
        "com.marry.api",
        "com.marry.domain",
        "com.marry.persistence",
        "com.marry.security",
        "com.marry.system",
        "com.marry.log",
        "com.marry.monitor",
        "com.marry.generator"
})
@MapperScan("com.marry.persistence.mapper")
public class MarryPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarryPlatformApplication.class, args);
        System.out.println("\n  🍵 marry-platform started at http://localhost:8080/api");
        System.out.println("  📘 Swagger UI:        http://localhost:8080/api/doc.html");
        System.out.println("  💚 Health check:      http://localhost:8080/api/actuator/health\n");
    }
}