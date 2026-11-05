package com.companybot.service;

import com.companybot.config.MailProperties;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final PasswordResetService passwordResetService;

    public MailService(JavaMailSender mailSender,
                       MailProperties mailProperties,
                       PasswordResetService passwordResetService) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
        this.passwordResetService = passwordResetService;
    }

    /** Equivalente ao email.php (formulário de contato). */
    public boolean enviarContato(String nome, String email, String telefone, String mensagem) {
        if (!mailProperties.possuiDestinatario()) {
            log.warn("Envio de contato ignorado: app.mail.to não configurado.");
            return false;
        }

        String corpo = """
                <p><strong>Nome:</strong> %s</p>
                <p><strong>E-mail:</strong> %s</p>
                <p><strong>Telefone:</strong> %s</p>
                <p><strong>Mensagem:</strong></p>
                <p>%s</p>
                """.formatted(
                escapar(nome),
                escapar(email),
                escapar(telefone),
                escapar(mensagem).replace("\n", "<br>"));

        return enviar(mailProperties.to(), "Pedido de Contato cliente: " + nome, corpo, true, mail -> {
            mail.setFrom(mailProperties.from(), "Email Automatico - Nao Responder");
            if (mailProperties.replyTo() != null && !mailProperties.replyTo().isBlank()) {
                mail.setReplyTo(mailProperties.replyTo());
            }
        });
    }

    /** E-mail do fluxo "esqueci minha senha". */
    public boolean enviarRecuperacao(String para, String nome) {
        String token = passwordResetService.gerarToken(para);
        String link = passwordResetService.link(token);
        String corpo = """
                <p>Olá %s,</p>
                <p>Recebemos uma solicitação de redefinição de senha para sua conta.</p>
                <p>Se não foi você, ignore este e-mail; sua senha atual continua válida.</p>
                <p>Para criar uma nova senha, acesse: <a href="%s">%s</a></p>
                <p>Este link expira em %d minutos.</p>
                """.formatted(escapar(nome), escapar(link), escapar(link),
                passwordResetService.validadeEmMinutos());

        boolean enviado = enviar(para, "Redefinição de senha - CompanyBot", corpo, true, mail ->
                mail.setFrom(mailProperties.from(), "CompanyBot"));
        if (!enviado && mailProperties.logLinksHabilitado()) {
            log.warn("Link de redefinição para {}: {}", para, link);
        }
        return enviado;
    }

    private boolean enviar(String para, String assunto, String corpo, boolean html, MailConfig configurator) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(para);
            helper.setSubject(assunto);
            configurator.configure(helper);
            helper.setText(corpo, html);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail para {} com assunto '{}'.", para, assunto, e);
            return false;
        }
    }

    private String escapar(String valor) {
        return valor == null ? "" : HtmlUtils.htmlEscape(valor);
    }

    @FunctionalInterface
    private interface MailConfig {
        void configure(MimeMessageHelper helper) throws Exception;
    }
}
