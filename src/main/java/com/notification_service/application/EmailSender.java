package com.notification_service.application;

public interface EmailSender {
    void send(String to, String subject, String body);
}
