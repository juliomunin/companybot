package com.companybot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.reset")
public record ResetProperties(String secret, Integer expirationMinutes) {

    public int expirationMinutesOuPadrao() {
        return expirationMinutes == null || expirationMinutes <= 0 ? 30 : expirationMinutes;
    }
}
