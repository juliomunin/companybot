package com.companybot.web;

import com.companybot.config.SiteProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;

/**
 * Atributos comuns a todas as páginas, evitando repetição nos controllers.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final SiteProperties siteProperties;

    public GlobalModelAttributes(SiteProperties siteProperties) {
        this.siteProperties = siteProperties;
    }

    @ModelAttribute("currentYear")
    public int currentYear() {
        return LocalDate.now().getYear();
    }

    @ModelAttribute("siteName")
    public String siteName() {
        return siteProperties.name() == null || siteProperties.name().isBlank()
                ? "CompanyBot"
                : siteProperties.name();
    }
}
