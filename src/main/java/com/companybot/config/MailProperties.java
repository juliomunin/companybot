package com.companybot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(String to, String from, String replyTo, Boolean logLinks) {

    public boolean possuiDestinatario() {
        return to != null && !to.isBlank();
    }

    public boolean logLinksHabilitado() {
        return Boolean.TRUE.equals(logLinks);
    }
}
