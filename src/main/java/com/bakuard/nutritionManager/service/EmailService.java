package com.bakuard.nutritionManager.service;

import com.bakuard.nutritionManager.config.configData.ConfigData;
import com.bakuard.nutritionManager.validation.*;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static com.bakuard.nutritionManager.validation.Rule.failure;

public class EmailService {

    private final ConfigData configData;

    public EmailService(ConfigData configData) {
        this.configData = configData;
    }

    public void confirmEmailForRegistration(String jws, String email) throws ValidateException {
        try {
            sendEmail("/mail/registration.html", jws, email);
        } catch(MessagingException e) {
            throw new ValidateException("Fail to confirm email for registration.", e).
                    addReason(Rule.of("EmailService.registration", failure(Constraint.SUCCESSFUL_MAIL_SENDING)));
        }
    }

    public void confirmEmailForChangeCredentials(String jws, String email) throws ValidateException {
        try {
            sendEmail("/mail/changeCredentials.html", jws, email);
        } catch(MessagingException e) {
            throw new ValidateException("Fail to confirm email for change credentials.", e).
                    addReason(Rule.of("EmailService.changeCredentials", failure(Constraint.SUCCESSFUL_MAIL_SENDING)));
        }
    }

    private void sendEmail(String htmlFileName, String jws, String email) throws MessagingException {
        Properties properties = new Properties();
        properties.setProperty("confirmationMail.transport.protocol", "smtps");
        properties.setProperty("confirmationMail.smtp.host", "smtp.gmail.com");
        properties.setProperty("confirmationMail.smtp.port", "465");
        properties.setProperty("confirmationMail.smtp.auth", "true");
        properties.setProperty("confirmationMail.smtp.socketFactory.port", "465");
        properties.setProperty("confirmationMail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        configData.confirmationMail().server(),
                        configData.confirmationMail().password()
                );
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(configData.confirmationMail().server()));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setSubject("Nutrition Manager");

        InputStream in = getClass().getResourceAsStream(htmlFileName);
        String html = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).
                lines().
                reduce(String::concat).
                orElseThrow().
                replaceAll("TOKEN", jws);
        message.setContent(html, "text/html; charset=utf-8");

        Transport.send(message);
    }

}
