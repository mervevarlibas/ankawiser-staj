package com.mwitter.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.mwitter.model.Notification;

public interface NotificationRepository extends MongoRepository<Notification, String> { //Notification modelini MongoDB CRUD işlemlerine bağlar.
    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(String recipientUserId); //Bildirim sayfası için yalnızca ilgili kullanıcının geçmişini en yeniden eskiye getirir.
    List<Notification> findByRecipientUserIdAndReadFalse(String recipientUserId); //markAllAsRead metoduna yalnızca okunmamış kayıtları getirir.
    long countByRecipientUserIdAndReadFalse(String recipientUserId); //Sol menü rozetindeki sayıyı liste çekmeden doğrudan MongoDB'de hesaplar.
}
