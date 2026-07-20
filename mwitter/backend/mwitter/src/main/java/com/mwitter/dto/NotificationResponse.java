package com.mwitter.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //Controller'ın JSON'a çevireceği alanların getter/setter metotlarını üretir.
@NoArgsConstructor //Jackson'ın DTO oluşturabilmesi için boş constructor üretir.
@AllArgsConstructor //NotificationService'in bütün response alanlarını tek seferde doldurmasını sağlar.
public class NotificationResponse {
    private String id; //Frontend'in bildirimi benzersiz olarak tanıyacağı Notification id'sidir.
    private String actorUserId; //FOLLOW bildirimi tıklanınca actor profilini açmak için frontend'e gider.
    private String actorUsername; //Bildirimi yapan kişinin kullanıcı adını ekranda göstermek için gider.
    private String type; //Frontend'in bildirime tıklanınca profil mi post mu açacağına karar vermesini sağlar.
    private String postId; //LIKE, COMMENT, REPOST ve MENTION bildirimlerinde post.html bağlantısını oluşturur.
    private String message; //İnsan tarafından okunacak Türkçe cümleyi backend hazırlar; frontend çeviri yapmaz.
    private LocalDateTime createdAt; //Frontend'in bildirim tarihini göstermesi için gönderilir.
    private boolean read; //Frontend'in okunmamış bildirimi farklı stille gösterebilmesi için gönderilir.
}
