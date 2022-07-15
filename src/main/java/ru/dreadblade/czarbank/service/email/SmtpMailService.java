package ru.dreadblade.czarbank.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@Profile("smtp")
@RequiredArgsConstructor
public class SmtpMailService implements MailService {
    private final MailSender mailSender;

    @Override
    public void sendMail(String recipientEmailAddress, String subject, String content) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setTo(recipientEmailAddress);
        mailMessage.setSubject(subject);
        mailMessage.setText(content);

        mailSender.send(mailMessage);
    }
}
