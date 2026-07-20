package com.mwitter.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.mwitter.dto.NotificationResponse;
import com.mwitter.model.Notification;
import com.mwitter.model.User;
import com.mwitter.repository.NotificationRepository;
import com.mwitter.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service //Bu sınıfı Spring'in iş kuralları katmanında yönetilen bir bean haline getirir.
@RequiredArgsConstructor //Final repository ve WebSocket alanlarını constructor ile Spring'e enjekte ettirir.
public class NotificationService {
    private final NotificationRepository notificationRepository; //Bildirimleri MongoDB'ye kaydetme, listeleme ve güncelleme işlemlerine bağlanır.
    private final UserRepository userRepository; //actorUserId üzerinden kullanıcı adını bulup response mesajı üretmek için kullanılır.
    private final SimpMessagingTemplate messagingTemplate; //WebSocketConfig'teki kişisel /user/queue kanallarına backend'den veri gönderir.

    public void notify(String recipientUserId, String actorUserId, String type, String postId) { //Follow/Post/Comment servislerinin ortak bildirim oluşturma giriş noktasıdır.
        if (recipientUserId.equals(actorUserId)) return; //Kullanıcı kendi postunu beğenir veya kendini etiketlerse kendisine bildirim göndermez.

        Notification notification = new Notification(); //MongoDB'ye kaydedilecek yeni Notification modelini oluşturur.
        notification.setRecipientUserId(recipientUserId); //Bildirimin hangi kullanıcının geçmişine ve WebSocket kanalına ait olduğunu belirler.
        notification.setActorUserId(actorUserId); //İşlemi yapan kullanıcıyı mesaja ve profil bağlantısına bağlar.
        notification.setType(type); //Mesaj üretiminde kullanılacak FOLLOW/LIKE/COMMENT/REPOST/MENTION türünü saklar.
        notification.setPostId(postId); //Frontend'in ilgili posta gidebilmesi için post id'sini saklar.
        notification.setCreatedAt(LocalDateTime.now()); //Tarihi frontend yerine güvenilir backend saatinden verir.
        notification.setRead(false); //Yeni kayıt sol menüdeki okunmamış sayısına dahil olsun diye false başlar.

        Notification saved = notificationRepository.save(notification); //Önce kalıcı olarak MongoDB'ye yazar; kullanıcı çevrimdışı olsa da bildirim kaybolmaz.
        NotificationResponse response = convertToResponse(saved); //Kaydedilen modeli frontend ve WebSocket için güvenli DTO'ya dönüştürür.
        messagingTemplate.convertAndSendToUser(recipientUserId, "/queue/notifications", response); //DM altyapısındaki aynı bağlantıyla yalnızca alıcının kişisel bildirim kanalına yollar.
    }

    public List<NotificationResponse> getNotifications(String userId) { //NotificationController'daki GET /notifications endpoint'i bu metodu çağırır.
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId).stream() //Repository'den giriş yapan kullanıcının sıralı geçmişini alır.
                .map(this::convertToResponse) //Her MongoDB modelini frontend'e gönderilecek NotificationResponse biçimine çevirir.
                .toList(); //Dönüştürülen DTO'ları controller'a liste olarak döndürür.
    }

    public long getUnreadCount(String userId) { //NotificationController'daki unread-count endpoint'ine sayı sağlar.
        return notificationRepository.countByRecipientUserIdAndReadFalse(userId); //Listeyi Java'ya taşımadan MongoDB'nin doğrudan saymasını sağlar.
    }

    public void markAllAsRead(String userId) { //Bildirim sayfası açıldığında ilgili kullanıcının bildirimlerini okundu yapar.
        List<Notification> unread = notificationRepository.findByRecipientUserIdAndReadFalse(userId); //Gereksiz kayıt güncellememek için yalnızca false olanları getirir.
        unread.forEach(notification -> notification.setRead(true)); //Bellekteki her okunmamış Notification modelini true yapar.
        if (!unread.isEmpty()) notificationRepository.saveAll(unread); //Değişen kayıtları MongoDB'ye tek toplu işlemle yazar.
    }

    private NotificationResponse convertToResponse(Notification notification) { //MongoDB modelini kullanıcı adı ve hazır mesaj içeren DTO'ya dönüştürür.
        User actor = userRepository.findById(notification.getActorUserId()).orElse(null); //Aktör silinmişse bütün geçmiş hata vermesin diye null kabul eder.
        String username = actor == null ? "Silinmiş kullanıcı" : actor.getUsername(); //Silinen aktör için güvenli görünen bir ad üretir.
        String message = switch (notification.getType()) { //Saklanan type değerini frontend'in doğrudan göstereceği Türkçe cümleye çevirir.
            case "FOLLOW" -> username + " seni takip etti"; //UserService.followUser tarafından üretilen olayın mesajıdır.
            case "LIKE" -> username + " gönderini beğendi"; //PostService.likePost tarafından üretilen olayın mesajıdır.
            case "COMMENT" -> username + " gönderine yorum yaptı"; //CommentService.createComment tarafından üretilen olayın mesajıdır.
            case "REPOST" -> username + " gönderini repostladı"; //PostService.repostPost tarafından üretilen olayın mesajıdır.
            case "MENTION" -> username + " senden bir gönderide bahsetti"; //Post veya yorum içindeki MentionService sonucundan üretilen olayın mesajıdır.
            default -> username + " bir işlem yaptı"; //Bilinmeyen bir tür gelirse response üretiminin tamamen bozulmasını önler.
        };
        return new NotificationResponse(notification.getId(), notification.getActorUserId(), username, //Kimlik ve actor bilgilerini DTO'ya taşır.
                notification.getType(), notification.getPostId(), message, notification.getCreatedAt(), //Yönlendirme, metin ve tarih alanlarını frontend'e taşır.
                notification.isRead()); //Okundu bilgisini listenin görünümü ve rozet hesabı için taşır.
    }
}
