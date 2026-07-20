package com.mwitter.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //Alanların getter/setter metotlarını Lombok üretir; service ve MongoDB bu metotları kullanır.
@NoArgsConstructor //MongoDB dokümanını Java nesnesine çevirirken gereken boş constructor'ı üretir.
@AllArgsConstructor //Test veya elle nesne üretirken bütün alanları alan constructor'ı üretir.
@Document(collection = "notifications") //Bu modelin MongoDB'deki notifications koleksiyonuna bağlandığını belirtir.
@CompoundIndex(name = "recipient_created_at_idx", def = "{'recipientUserId': 1, 'createdAt': -1}") //Kullanıcının geçmişini en yeniden eskiye hızlı getiren indekstir.
@CompoundIndex(name = "recipient_read_idx", def = "{'recipientUserId': 1, 'read': 1}") //Kullanıcıya ait okunmamış kayıtları sayma sorgusunu hızlandırır.
public class Notification {
    @Id
    private String id; //MongoDB'nin her bildirim için ürettiği benzersiz doküman kimliğidir.
    private String recipientUserId; //Bildirimi alacak User kaydının id'sidir; kişisel liste ve WebSocket hedefi bununla belirlenir.
    private String actorUserId; //Takip, beğeni, yorum gibi işlemi yapan User kaydının id'sidir.
    private String type; //FOLLOW, LIKE, COMMENT, REPOST veya MENTION olay türünü taşır.
    private String postId; //Postla ilgili bildirimlerde Post kaydına yönlendirme yapmak için kullanılır; FOLLOW işleminde null olur.
    private LocalDateTime createdAt; //Bildirimin backend tarafından oluşturulduğu zamanı tutar ve sıralamada kullanılır.
    private boolean read; //Frontend rozeti için bildirimin okunup okunmadığını belirtir.
}
