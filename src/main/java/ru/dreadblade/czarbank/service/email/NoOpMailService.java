package ru.dreadblade.czarbank.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnMissingBean(SmtpMailService.class)
public class NoOpMailService implements MailService {
    @Override
    public void sendMail(String recipientEmailAddress, String subject, String content) {
        log.info("Email sent to {}:\n{}\n{}", recipientEmailAddress, subject, content);
    }
}
