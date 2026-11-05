package com.companybot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CompanybotApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompanybotApplication.class, args);
    }
}