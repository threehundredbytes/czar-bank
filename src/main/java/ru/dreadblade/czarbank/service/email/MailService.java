package ru.dreadblade.czarbank.service.email;

public interface MailService {
    void sendMail(String recipientEmailAddress, String subject, String content);
}
