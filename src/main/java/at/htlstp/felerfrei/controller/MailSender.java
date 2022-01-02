package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.domain.user.User;
import at.htlstp.felerfrei.persistence.VerificationTokenRepository;
import lombok.SneakyThrows;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Component
public class MailSender {

    private final JavaMailSender mailSender;
    private final VerificationTokenRepository tokenRepository;

    public MailSender(JavaMailSender sender, VerificationTokenRepository tokenRepository) {
        this.mailSender = sender;
        this.tokenRepository = tokenRepository;
    }


    @SneakyThrows
    public void sendVerificationEmail(User user, String siteURL) {

        // read text from file and replace placeholders
        var text = new String(this.getClass().getResourceAsStream("/email/verification.html").readAllBytes());
        text = text.replace("{name}", user.getFirstname() + " " + user.getLastname());
        text = text.replace("{link}", siteURL + tokenRepository.findByUser(user).getToken());

        MimeMessage mailMessage = mailSender.createMimeMessage();
        mailMessage.setSubject("Verification", "UTF-8");

        var helper = new MimeMessageHelper(mailMessage, true, "UTF-8");
        helper.setTo(user.getEmail());
        helper.setText(text, true);

        mailSender.send(mailMessage);
    }
}
