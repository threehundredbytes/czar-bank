package ru.dreadblade.czarbank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final MailSender mailSender;

    @Autowired
    public MailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMail(String recipientEmailAddress, String subject, String content) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setTo(recipientEmailAddress);
        mailMessage.setSubject(subject);
        mailMessage.setText(content);

        mailSender.send(mailMessage);
    }
}
