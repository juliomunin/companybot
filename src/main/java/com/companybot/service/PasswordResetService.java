package com.companybot.service;

import com.companybot.config.ResetProperties;
import com.companybot.config.SiteProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * Gera e valida tokens de redefinição de senha sem persistir estado.
 * O token é assinado com HMAC-SHA256 e carrega o e-mail e a validade.
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final SiteProperties siteProperties;
    private final byte[] secret;
    private final Duration validade;
    private final Clock clock;

    @Autowired
    public PasswordResetService(ResetProperties properties, SiteProperties siteProperties) {
        this(properties, siteProperties, Clock.systemUTC());
    }

    PasswordResetService(ResetProperties properties, SiteProperties siteProperties, Clock clock) {
        this.siteProperties = siteProperties;
        this.clock = clock;
        this.validade = Duration.ofMinutes(properties.expirationMinutesOuPadrao());
        this.secret = segredo(properties.secret());
    }

    public String gerarToken(String email) {
        String normalizado = normalizar(email);
        if (normalizado == null) {
            throw new IllegalArgumentException("Informe um e-mail válido para redefinir a senha.");
        }
        long expiraEm = clock.instant().plus(validade).getEpochSecond();
        String payload = normalizado + "|" + expiraEm;
        String conteudo = payload + "|" + assinar(payload);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(conteudo.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<String> validarToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            String conteudo = new String(Base64.getUrlDecoder().decode(token.trim()),
                    StandardCharsets.UTF_8);
            int ultimo = conteudo.lastIndexOf('|');
            int penultimo = conteudo.lastIndexOf('|', ultimo - 1);
            if (penultimo <= 0 || ultimo <= penultimo) {
                return Optional.empty();
            }

            String email = conteudo.substring(0, penultimo);
            String expiraEmTexto = conteudo.substring(penultimo + 1, ultimo);
            String assinatura = conteudo.substring(ultimo + 1);
            long expiraEm = Long.parseLong(expiraEmTexto);

            String esperada = assinar(email + "|" + expiraEmTexto);
            if (!MessageDigest.isEqual(esperada.getBytes(StandardCharsets.UTF_8),
                    assinatura.getBytes(StandardCharsets.UTF_8))) {
                return Optional.empty();
            }
            if (clock.instant().getEpochSecond() > expiraEm) {
                return Optional.empty();
            }
            return Optional.of(email);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public String link(String token) {
        return siteProperties.urlOuPadrao() + "/reset?token=" + token;
    }

    public int validadeEmMinutos() {
        return (int) validade.toMinutes();
    }

    private String normalizar(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String assinar(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao assinar token de redefinição de senha.", e);
        }
    }

    private byte[] segredo(String configurado) {
        if (configurado != null && !configurado.isBlank()) {
            return configurado.getBytes(StandardCharsets.UTF_8);
        }
        byte[] aleatorio = new byte[32];
        new SecureRandom().nextBytes(aleatorio);
        log.warn("app.reset.secret não configurado; tokens serão invalidados ao reiniciar a aplicação.");
        return aleatorio;
    }
}
