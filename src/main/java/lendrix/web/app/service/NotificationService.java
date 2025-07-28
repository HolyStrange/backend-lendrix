package lendrix.web.app.service;

import lendrix.web.app.entity.User;

public interface NotificationService {
    void sendEmail(String to, String subject, String body);
    void sendSms(String to, String message);
    void sendInAppNotification(User user, String message);
} 