package lendrix.web.app.service;

import org.springframework.stereotype.Service;

import lendrix.web.app.entity.User;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Override
    public void sendEmail(String to, String subject, String body) {
        // TODO: Integrate with an email provider (JavaMailSender)
        System.out.printf("Email sent to %s: %s - %s%n", to, subject, body);
    }

    @Override
    public void sendSms(String to, String message) {
        // TODO: Integrate with an SMS provider (Twilio)
        System.out.printf("SMS sent to %s: %s%n", to, message);
    }

    @Override
    public void sendInAppNotification(User user, String message) {
        // TODO: Save notification to DB or push to frontend
        System.out.printf("In-app notification for %s: %s%n", user.getUsername(), message);
    }
} 