package com.mwitter.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mwitter.dto.MessageResponse;
import com.mwitter.dto.NotificationResponse;
import com.mwitter.service.NotificationService;
import lombok.RequiredArgsConstructor;

@RestController //Metot sonuçlarını frontend'e JSON HTTP cevabı olarak gönderir.
@RequestMapping("/notifications") //Bu controller içindeki bütün endpoint'lerin ortak URL başlangıcıdır.
@RequiredArgsConstructor //Final NotificationService alanını constructor üzerinden Spring'e enjekte ettirir.
public class NotificationController {
    private final NotificationService notificationService; //HTTP katmanını bildirim iş kurallarının bulunduğu service katmanına bağlar.

    @GetMapping //GET /notifications isteğini bildirim geçmişi metoduna bağlar.
    public List<NotificationResponse> getNotifications(Authentication authentication) {
        return notificationService.getNotifications(authentication.getName()); //JWT filtresinin Authentication içine koyduğu userId ile yalnızca o kullanıcının kayıtlarını getirir.
    }

    @GetMapping("/unread-count") //GET /notifications/unread-count isteğini sol menü rozeti için karşılar.
    public long getUnreadCount(Authentication authentication) {
        return notificationService.getUnreadCount(authentication.getName()); //JWT'deki userId'yi repository count sorgusuna service üzerinden yollar.
    }

    @PostMapping("/mark-read") //POST /notifications/mark-read isteğini bildirim sayfası açıldığında karşılar.
    public MessageResponse markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName()); //Sadece giriş yapan kullanıcının okunmamış bildirimlerini günceller.
        return new MessageResponse(LocalDateTime.now(), "Notifications marked as read."); //Frontend'e işlemin başarıyla tamamlandığını bildirir.
    }
}
