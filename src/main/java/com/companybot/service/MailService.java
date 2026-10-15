package com.companybot.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.to}")
    private String destinatario;

    @Value("${app.mail.from}")
    private String remetente;

    @Value("${app.mail.reply-to}")
    private String replyTo;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /** Equivalente ao email.php (formulário de contato). */
    public boolean enviarContato(String nome, String email, String telefone, String mensagem) {
        String corpo = "Mensagem: " + mensagem
            + "\r\n"
            + "Email: " + email
            + "\r\n"
            + "Telefone: " + telefone;

        return enviar(destinatario, "Pedido de Contato cliente: " + nome, corpo, mail -> {
            mail.setFrom(remetente, "Email Automatico - Nao Responder");
            mail.setReplyTo(replyTo, "Meliana");
        });
    }

    /** E-mail do fluxo "esqueci minha senha". */
    public boolean enviarRecuperacao(String para, String nome) {
        String corpo = "Olá " + nome + ",\r\n\r\n"
            + "Recebemos uma solicitação de recuperação de senha para sua conta.\r\n"
            + "Se não foi você, ignore este e-mail.\r\n"
            + "Para continuar, acesse a página de recuperação: ";

        return enviar(para, "Recuperação de senha - CompanyBot", corpo, mail ->
            mail.setFrom(remetente, "CompanyBot")
        );
    }

    private boolean enviar(String para, String assunto, String corpo, MailConfig configurator) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(para);
            helper.setSubject(assunto);
            configurator.configure(helper);
            helper.setText(corpo, true);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @FunctionalInterface
    private interface MailConfig {
        void configure(MimeMessageHelper helper) throws Exception;
    }
}