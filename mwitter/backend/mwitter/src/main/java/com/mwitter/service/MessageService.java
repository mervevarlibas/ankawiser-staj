package com.mwitter.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.mwitter.dto.ConversationResponse;
import com.mwitter.dto.MessageRequest;
import com.mwitter.dto.MessageResponse;
import com.mwitter.model.Message;
import com.mwitter.model.User;
import com.mwitter.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;//Spring'in "Canlı Yayın Aracı"dır. Veritabanına kaydedilen bir mesajı alıp, açık olan WebSocket borusundan anında kullanıcının ekranına (frontend'e) fırlatmaya yarar.sayfa yenilemeye gerek kalmaz

    public MessageResponse sendMessage(String senderId, MessageRequest request) {//Gönderen ve alan kişinin IDleri üzerinden kontroller yapılır
        User sender = userService.getUserById(senderId);
        User receiver = userService.getUserById(request.getReceiverId());

        if (senderId.equals(receiver.getId())) {
            throw new RuntimeException("You cannot send a message to yourself.");
        }

        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (content.isEmpty()) {
            throw new RuntimeException("Message cannot be empty.");
        }
        if (content.length() > 1000) {
            throw new RuntimeException("Message cannot exceed 1000 characters.");
        }

        Message message = new Message();//yeni message nesensi oluşup vtabanına kaydedilir
        message.setSenderId(senderId);
        message.setReceiverId(receiver.getId());
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        message.setRead(false);

        MessageResponse response = convertToResponse(
                messageRepository.save(message),
                sender,
                receiver
        );

        messagingTemplate.convertAndSendToUser(receiver.getId(), "/queue/messages", response);//WebSocketConfig dosyasında /queue çıkış kapısını ve /user kişisel odalarını ayarlamıştık.Bu mesajı al, receiver.getId() numaralı odanın içindeki /queue/messages anında anons et." Eğer karşı taraf o an sitedeyse, ekranında anında mesaj belirir
        messagingTemplate.convertAndSendToUser(sender.getId(), "/queue/messages", response);//gönderenin de attığı mesajı anında görebilmesi için
        return response;
    }

    public List<MessageResponse> getConversation(String userId, String otherUserId) {//Bu metot bir sohbete tıkladığında geçmişi getirir ve "Okundu" bilgisini ayarlar.
        User currentUser = userService.getUserById(userId);
        User otherUser = userService.getUserById(otherUserId);
        List<Message> messages = messageRepository.findConversation(userId, otherUserId);//ile iki kişi arasındaki tüm geçmişi çeker ve tarihe göre sıraya dizer.
        messages.sort(Comparator.comparing(Message::getSentAt));//Repositoryden gelen karışık geçmişi tarihe göre yukarıdan aşağıya sıralar

        List<Message> messagesToMarkAsRead = new ArrayList<>();
        List<MessageResponse> responses = new ArrayList<>();

        for (Message message : messages) {//Döngü içinde gelen her mesaja bakar. Eğer bu mesajı alan kişi şu an bu metodu çağıran kişiyse (userId.equals(message.getReceiverId())) ve mesaj henüz okunmamışsa (!message.isRead()), anında okundu olarak işaretler (setRead(true)).
            if (userId.equals(message.getReceiverId()) && !message.isRead()) {
                message.setRead(true);
                messagesToMarkAsRead.add(message);
            }

            User sender = userId.equals(message.getSenderId()) ? currentUser : otherUser;
            User receiver = userId.equals(message.getReceiverId()) ? currentUser : otherUser;
            responses.add(convertToResponse(message, sender, receiver));
        }

        if (!messagesToMarkAsRead.isEmpty()) {
            messageRepository.saveAll(messagesToMarkAsRead);//ile okunan tüm mesajları tek seferde veritabanında günceller.
        }
        return responses;
    }

    public List<ConversationResponse> getConversationList(String userId) {//gelen kutusu
        userService.getUserById(userId);
        List<Message> messages = messageRepository
                .findBySenderIdOrReceiverIdOrderBySentAtDesc(userId, userId);

        Map<String, Message> latestMessageByUser = new LinkedHashMap<>();//LinkedHashMap eklenme sırasını unutmayan özel bir sözlük tipidir
        Map<String, Long> unreadCountByUser = new LinkedHashMap<>();

        for (Message message : messages) {
            String otherUserId = userId.equals(message.getSenderId())
                    ? message.getReceiverId()
                    : message.getSenderId();

            latestMessageByUser.putIfAbsent(otherUserId, message);//Yüzlerce mesajın içinden sadece her kullanıcıyla olan en son mesajı bulup atar
            if (userId.equals(message.getReceiverId()) && !message.isRead()) {
                unreadCountByUser.merge(otherUserId, 1L, Long::sum);//içindeki bildirim sayısının hesaplandığı yer. okunmayan mesajları sayıp
            }
        }

        List<ConversationResponse> responses = new ArrayList<>();
        for (Map.Entry<String, Message> entry : latestMessageByUser.entrySet()) {
            User otherUser;
            try {
                otherUser = userService.getUserById(entry.getKey());
            } catch (RuntimeException exception) {
                continue;
            }

            Message lastMessage = entry.getValue();
            responses.add(new ConversationResponse(
                    otherUser.getId(),
                    otherUser.getUsername(),
                    lastMessage.getContent(),
                    lastMessage.getSentAt(),
                    userId.equals(lastMessage.getSenderId()),
                    unreadCountByUser.getOrDefault(otherUser.getId(), 0L)
            ));
        }
        return responses;
    }

    private MessageResponse convertToResponse(Message message, User sender, User receiver) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSenderId());
        response.setSenderUsername(sender.getUsername());
        response.setReceiverId(message.getReceiverId());
        response.setReceiverUsername(receiver.getUsername());
        response.setContent(message.getContent());
        response.setSentAt(message.getSentAt());
        response.setRead(message.isRead());
        return response;
    }
}
