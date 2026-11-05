package com.companybot.service;

import com.companybot.config.ResetProperties;
import com.companybot.config.SiteProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordResetServiceTest {

    private static final ResetProperties PROPRIEDADES = new ResetProperties("segredo-de-teste", 30);
    private static final SiteProperties SITE = new SiteProperties("CompanyBot", "https://example.com");
    private static final Clock AGORA = Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneOffset.UTC);

    private PasswordResetService servico(ResetProperties propriedades, Clock clock) {
        return new PasswordResetService(propriedades, SITE, clock);
    }

    @Test
    void geraEValidaTokenNormalizandoEmail() {
        PasswordResetService service = servico(PROPRIEDADES, AGORA);

        String token = service.gerarToken("  Joao@Example.com ");

        assertThat(service.validarToken(token)).contains("joao@example.com");
        assertThat(service.link(token)).isEqualTo("https://example.com/reset?token=" + token);
        assertThat(service.validadeEmMinutos()).isEqualTo(30);
    }

    @Test
    void tokenAdulteradoNaoValida() {
        PasswordResetService service = servico(PROPRIEDADES, AGORA);
        String token = service.gerarToken("joao@example.com");

        assertThat(service.validarToken(token + "x")).isEmpty();
        assertThat(service.validarToken("nao-e-um-token")).isEmpty();
        assertThat(service.validarToken(" ")).isEmpty();
        assertThat(service.validarToken(null)).isEmpty();
    }

    @Test
    void tokenExpiradoNaoValida() {
        String token = servico(PROPRIEDADES, AGORA).gerarToken("joao@example.com");
        Clock depois = Clock.fixed(AGORA.instant().plus(Duration.ofMinutes(31)), ZoneOffset.UTC);

        assertThat(servico(PROPRIEDADES, depois).validarToken(token)).isEmpty();
    }

    @Test
    void tokenDeOutroSegredoNaoValida() {
        String token = servico(PROPRIEDADES, AGORA).gerarToken("joao@example.com");
        ResetProperties outroSegredo = new ResetProperties("outro-segredo", 30);

        assertThat(servico(outroSegredo, AGORA).validarToken(token)).isEmpty();
    }

    @Test
    void emailVazioNaoGeraToken() {
        PasswordResetService service = servico(PROPRIEDADES, AGORA);

        assertThatThrownBy(() -> service.gerarToken(" "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.gerarToken(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
