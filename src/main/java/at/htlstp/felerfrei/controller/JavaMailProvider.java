package at.htlstp.felerfrei.controller;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@Configuration
public class JavaMailProvider {

    @Bean
    public JavaMailSender getJavaMailSender() throws URISyntaxException {
        var mailSender = new JavaMailSenderImpl();
        var properties = getConfiguration(Paths.get(getClass().getResource("/email.config").toURI()).toString());
        mailSender.setHost(properties.get("host").toString());
        var port = Integer.parseInt(String.valueOf(properties.get("port")));
        mailSender.setPort(port);
        mailSender.setUsername(properties.get("username").toString());
//        mailSender.setPassword(properties.get("password").toString());
        var javaMailProperties = mailSender.getJavaMailProperties();
//        javaMailProperties.setProperty("mail.transport.protocol", "smtp");
//        javaMailProperties.setProperty("mail.smtp.auth", "true");
//        javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");

        return mailSender;
    }

    @SneakyThrows
    private Properties getConfiguration(String filename) {
        var properties = new Properties();
        try (var lines = Files.lines(Paths.get(filename))) {
            lines.map(line -> line.split("="))
                    .map(parts -> new String[]{parts[0], parts[1]})
                    .forEach(parts -> properties.put(parts[0], parts[1]));
            return properties;
        }
    }
}
