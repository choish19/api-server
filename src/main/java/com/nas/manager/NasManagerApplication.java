package com.nas.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.nas.manager.config.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    FileStorageProperties.class
})
public class NasManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(NasManagerApplication.class, args);
    }
}