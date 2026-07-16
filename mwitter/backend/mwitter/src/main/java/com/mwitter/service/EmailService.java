package com.mwitter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender; // Spring, application.properties'teki
                                             // ayarlara göre bu bean'i otomatik oluşturur

    @Value("${spring.mail.username}")
    private String fromAddress; // gönderen adres, config'ten okunuyor

    public void sendVerificationMail(String toEmail, String code) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Mwitter - E-posta Doğrulama Kodu");
        message.setText("Kayıt işlemini tamamlamak için doğrulama kodun: " + code);

        mailSender.send(message);
    }
}