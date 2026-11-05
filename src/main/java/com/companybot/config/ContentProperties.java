package com.companybot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.content")
public record ContentProperties(String aulas) {

    public String aulasOuPadrao() {
        return aulas == null || aulas.isBlank() ? "conteudo/aulas.json" : aulas;
    }
}
