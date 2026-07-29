package com.marry.persistence.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

/**
 * Programmatic Flyway configuration. Ensures Flyway runs before MyBatis-Plus initializes.
 * Spring Boot's auto-configuration handles most setup; this is mainly used to attach
 * custom callbacks if needed in future.
 */
@Configuration
@EnableConfigurationProperties(FlywayProperties.class)
public class FlywayConfig {

    @Bean
    @DependsOn("dataSource")
    public Flyway flyway(DataSource dataSource, FlywayProperties properties) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(properties.getLocations().toArray(new String[0]))
                .baselineOnMigrate(properties.isBaselineOnMigrate())
                .validateOnMigrate(properties.isValidateOnMigrate())
                .load();
    }
}