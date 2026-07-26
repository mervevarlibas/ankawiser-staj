package com.mwitter.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final GmailApiMailService gmailApiMailService;

    public void sendVerificationMail(String toEmail, String code) {
        try {
            gmailApiMailService.sendEmail(
                    toEmail,
                    "Mwitter - E-posta Doğrulama Kodu",
                    "Kayıt işlemini tamamlamak için doğrulama kodun: " + code);
        } catch (RuntimeException ex) {
            throw new RuntimeException(
                    "Doğrulama e-postası gönderilemedi. Lütfen daha sonra tekrar dene.", ex);
        }
    }

    public void sendPasswordResetMail(String toEmail, String resetLink) {
        gmailApiMailService.sendEmail(
                toEmail,
                "Mwitter - Şifre Sıfırlama",
                "Şifreni sıfırlamak için aşağıdaki bağlantıya tıkla:\n\n"
                        + resetLink
                        + "\n\nBu bağlantı 15 dakika geçerlidir. Bu isteği sen yapmadıysan e-postayı görmezden gelebilirsin.");
    }
}
