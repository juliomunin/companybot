package com.companybot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.maps")
public record MapsProperties(String apiKey) {

    public boolean habilitado() {
        return apiKey != null && !apiKey.isBlank();
    }
}
