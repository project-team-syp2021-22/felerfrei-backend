package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.domain.user.User;
import at.htlstp.felerfrei.persistence.VerificationTokenRepository;
import lombok.SneakyThrows;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

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
        var message = new SimpleMailMessage();

        message.setTo(user.getEmail());
        message.setSubject("Verification");
        message.setText("To verify your account, please click here: " + siteURL + tokenRepository.findByUser(user).getToken());
        mailSender.send(message);
    }
}
