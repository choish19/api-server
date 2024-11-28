package com.nas.manager.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public Path fileStorageLocation() {
        return Paths.get("C:/file-storage").toAbsolutePath().normalize();
    }
} 