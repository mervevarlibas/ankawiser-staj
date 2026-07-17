package com.mwitter.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mwitter.dto.ConversationResponse;
import com.mwitter.dto.MessageRequest;
import com.mwitter.dto.MessageResponse;
import com.mwitter.service.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Validated
public class MessageController {//Kullanıcı DM kutusuna ilk girdiğinde veya bir sohbete tıkladığında çalışır.Authentication authentication kullanılarak JWT token'dan kimlik alınır ve MessageService'ten sohbet geçmişi veya gelen kutusu özeti istenir

    private final MessageService messageService;

    @GetMapping("/conversations")//frontend yüklendiğinde sohbet geçmişini ver demek için atılan http isteği
    public List<ConversationResponse> getConversationList(Authentication authentication) {
        return messageService.getConversationList(authentication.getName());
    }

    @GetMapping("/{otherUserId}")
    public List<MessageResponse> getConversation(
            @PathVariable String otherUserId,
            Authentication authentication) {//üzerinden kullanıcının token i alınır ve Service metoduna fırlatılır
        return messageService.getConversation(authentication.getName(), otherUserId);
    }

    @MessageMapping("/chat.send") //@PostMapping yok çünkü http kullanılmıyor mesaj gönderirken. stomp paketi geliyorFrontend'deki JavaScript kodu, mesajı /app/chat.send yoluna fırlatıyor.Frontend paketi /app/chat.send adresine fırlattığında, paket doğrudan buraya düşer.
    public void sendMessage(@Valid MessageRequest request, Principal principal) {//WebSocketAuthInterceptor dosyasında  paket buraya principal principal olarak düşüyor
        if (principal == null) {
            throw new RuntimeException("WebSocket user is not authenticated.");
        }
        messageService.sendMessage(principal.getName(), request);//ile kimliği alır, mesaj içeriğini alır ve sendmessage e yollar
    }
}
