package com.mwitter.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mwitter.model.Message;

public interface MessageRepository extends MongoRepository<Message, String> {//Message modelini MongoDB'ye bağlayıp standart kaydetme (save), silme (delete) yeteneklerini kazandırır.

    @Query("{ $or: [ " //MongoDB JSON sorgusudur.Bana şu iki şarttan herhangi birini ($or) sağlayan mesajları getir:Gönderen 1. kişi (?0) VE alan 2. kişi (?1) olsun. yani benim ona attıklarım
            + "{ 'senderId': ?0, 'receiverId': ?1 }, "
            + "{ 'senderId': ?1, 'receiverId': ?0 } "//2. kişi (?1) VE alan 1. kişi (?0) olsun. (Yani onun sana attıkları)
            + "] }")
    List<Message> findConversation(String userId1, String userId2);//Bu metoda verilen iki ID, yukarıdaki sorgudaki ?0 ve ?1 yerlerine geçer. Sonuç olarak iki kişi arasındaki tüm mesajlaşma geçmişi tek bir liste olarak gelir.

    List<Message> findBySenderIdOrReceiverIdOrderBySentAtDesc(//findBy (Bul) -> SenderId (Göndereni şu olan) -> Or (VEYA) -> ReceiverId (Alıcısı şu olan) -> OrderBySentAtDesc (Ve bunları tarihe göre en yeniden en eskiye doğru sırala).
            String senderId,
            String receiverId
    );
}
