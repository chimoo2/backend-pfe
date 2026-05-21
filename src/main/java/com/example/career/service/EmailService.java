package com.example.career.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:no-reply@careersavvy.com}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordEmail(String to, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Réinitialisation de votre mot de passe");

            String html = "<p>Bonjour,</p>" +
                    "<p>Vous avez demandé à réinitialiser votre mot de passe. Cliquez sur le lien ci-dessous :</p>" +
                    "<p><a href=\"" + resetLink + "\">Réinitialiser mon mot de passe</a></p>" +
                    "<p>Si vous n'avez pas demandé cette action, vous pouvez simplement ignorer ce message.</p>" +
                    "<p>Cordialement,<br/>L'équipe CapTalent</p>";

            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException ex) {
            log.error("Failed to send reset password email to {}", to, ex);
        }
    }
}
