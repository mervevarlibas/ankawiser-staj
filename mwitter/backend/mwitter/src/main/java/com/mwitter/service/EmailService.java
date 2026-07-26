package com.mwitter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender; // Spring, application.properties'teki
                                             // ayarlara göre bu bean'i otomatik oluşturur

    @Value("${app.mail.from:${spring.mail.username}}")
    private String fromAddress; // gönderen adres, config'ten okunuyor

    public void sendVerificationMail(String toEmail, String code) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Mwitter - E-posta Doğrulama Kodu");
        message.setText("Kayıt işlemini tamamlamak için doğrulama kodun: " + code);

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Verification email could not be sent to {}", toEmail, ex);
            throw new RuntimeException(
                    "Doğrulama e-postası gönderilemedi. Lütfen daha sonra tekrar dene.", ex);
        }
    }

    public void sendPasswordResetMail(String toEmail, String resetLink) {//UserService'in ürettiği tek kullanımlık linki Spring JavaMailSender ile kullanıcıya gönderir.
        SimpleMailMessage message = new SimpleMailMessage();//Spring Mail'in düz metin e-posta modelini oluşturur.
        message.setFrom(fromAddress);//application.properties içindeki spring.mail.username adresini gönderen olarak kullanır.
        message.setTo(toEmail);//UserRepository'den bulunan kullanıcının kayıtlı e-posta adresini alıcı yapar.
        message.setSubject("Mwitter - Şifre Sıfırlama");//Kullanıcının gelen kutusunda göreceği şifre sıfırlama mail başlığını belirler.
        message.setText("Şifreni sıfırlamak için aşağıdaki bağlantıya tıkla:\n\n"
                + resetLink
                + "\n\nBu bağlantı 15 dakika geçerlidir. Bu isteği sen yapmadıysan e-postayı görmezden gelebilirsin.");//Reset linkini, süresini ve güvenlik uyarısını mail gövdesine ekler.
        mailSender.send(message);//Hazırlanan maili application.properties SMTP ayarları üzerinden gerçek mail sunucusuna yollar.
    }
}
