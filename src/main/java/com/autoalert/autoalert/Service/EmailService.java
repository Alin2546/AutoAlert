package com.autoalert.autoalert.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String code) {
        String subject = "Verificare cont AutoAlert";
        String content = "<h2>Salut!</h2>" +
                "<p>Codul tău de verificare este: <b style='font-size:18px;'>" + code + "</b></p>" +
                "<p>Introdu-l în aplicație pentru a-ți activa contul.</p>" +
                "<br><p>AutoAlert 🚗</p>";

        sendHtmlEmail(to, subject, content);
    }

    public void sendResetPasswordEmail(String to, String resetLink) {
        String subject = "Resetare parolă AutoAlert";
        String content = "<h2>Ai cerut resetarea parolei</h2>" +
                "<p>Dă click pe link-ul de mai jos pentru a-ți reseta parola:</p>" +
                "<a href=\"" + resetLink + "\">Resetează parola</a>" +
                "<br><br><p>Dacă nu ai cerut tu această acțiune, ignoră acest email.</p>" +
                "<br><p>AutoAlert 🚗</p>";

        sendHtmlEmail(to, subject, content);
    }

    public void sendExpiryNotificationEmail(String to, String documentType, String carBrand, String carModel, LocalDate expiryDate, int daysLeft) {
        String subject = "Reminder: Document " + documentType + " expiră în curând";
        String content = "<h2>Salut!</h2>" +
                "<p>Documentul <b>" + documentType + "</b> pentru mașina ta " +
                carBrand + " " + carModel + " expiră peste <b>" + daysLeft + " zile</b>, pe data de " +
                expiryDate.toString() + ".</p>" +
                "<p>Te rugăm să îl reînnoiești pentru a evita problemele.</p>" +
                "<br><p>AutoAlert 🚗</p>";

        sendHtmlEmail(to, subject, content);
    }


    public void sendPasswordChangedConfirmationEmail(String to) {
        String subject = "Parolă schimbată cu succes";
        String content = "<h2>Salut!</h2>" +
                "<p>Parola contului tău AutoAlert a fost schimbată cu succes.</p>" +
                "<p>Dacă nu ai fost tu, resetează parola imediat!</p>" +
                "<br><p>AutoAlert 🚗</p>";

        sendHtmlEmail(to, subject, content);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@autoalert.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Eroare la trimiterea emailului: " + e.getMessage());
        }
    }
}
