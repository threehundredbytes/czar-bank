package ru.dreadblade.czarbank.service.email;

import javax.mail.MessagingException;

public interface MailService {
    void sendHtmlMail(String recipientEmailAddress, String subject, String htmlContent) throws MessagingException;
}
