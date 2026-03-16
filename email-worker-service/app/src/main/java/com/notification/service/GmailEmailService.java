package com.notification.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class GmailEmailService {

    public void send(String to, String subject, String body) throws Exception {

        final String from = System.getenv("GMAIL_USERNAME");
        final String password = System.getenv("GMAIL_APP_PASSWORD");

        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, password);
                    }
                });

        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(from));

        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(to));

        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}