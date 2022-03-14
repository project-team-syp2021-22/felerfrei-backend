package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.domain.user.VerificationToken;
import lombok.SneakyThrows;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.util.Objects;

@Component
public class MailSender {

    private final JavaMailSender javaMailSender;
    private static final String CHARSET = "UTF-8";

    public MailSender(JavaMailSender sender) {
        this.javaMailSender = sender;
    }


    @SneakyThrows
    public void sendVerificationEmail(VerificationToken token, String siteURL) {
        var user = token.getUser();

        // read text from file and replace placeholders
        var text = new String(Objects.requireNonNull(this.getClass().getResourceAsStream("/email/verification.html")).readAllBytes());
        text = text.replace("{name}", user.getFirstname() + " " + user.getLastname());
        text = text.replace("{link}", siteURL + token);

        MimeMessage mailMessage = javaMailSender.createMimeMessage();
        mailMessage.setSubject("Verification", CHARSET);

        var helper = new MimeMessageHelper(mailMessage, true, CHARSET);
        helper.setTo(user.getEmail());
        helper.setText(text, true);

        new Thread(() -> javaMailSender.send(mailMessage)).start();
    }

    @SneakyThrows
    public void sendPasswordResetEmail(VerificationToken token, String siteURL) {
        var user = token.getUser();

        // read text from file and replace placeholders
        var text = new String(Objects.requireNonNull(this.getClass().getResourceAsStream("/email/reset.html")).readAllBytes());
        text = text.replace("{name}", user.getFirstname() + " " + user.getLastname());
        text = text.replace("{link}", siteURL + token);

        MimeMessage mailMessage = javaMailSender.createMimeMessage();
        mailMessage.setSubject("Reset your password!", CHARSET);

        var helper = new MimeMessageHelper(mailMessage, true, CHARSET);
        helper.setTo(user.getEmail());
        helper.setText(text, true);
        new Thread(() -> javaMailSender.send(mailMessage)).start();
    }
}
