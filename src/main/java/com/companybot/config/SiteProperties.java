package com.companybot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.site")
public record SiteProperties(String name, String url) {

    public String urlOuPadrao() {
        return url == null || url.isBlank() ? "https://companybot.com.br" : url;
    }
}
