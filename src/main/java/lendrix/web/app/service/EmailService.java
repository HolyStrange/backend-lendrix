// package lendrix.web.app.service;

// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.stereotype.Service;

// import lombok.RequiredArgsConstructor;



// @Service
// @RequiredArgsConstructor
// public class EmailService {

//     private final JavaMailSender mailSender;

//     public void sendSimpleEmail(String to, String subject, String text) {
//         SimpleMailMessage message = new SimpleMailMessage();
//         message.setFrom("lendrixapp@gmail.com");
//         message.setTo(to);
//         message.setSubject(subject);
//         message.setText(text);

//         mailSender.send(message);
//     }
// }
