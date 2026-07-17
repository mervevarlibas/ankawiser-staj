package com.mwitter.security;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
//http istekleri için jwtauthenticationfilter var.ama o filtre websocket içinden akan anlık mesajları kontrol edemez bu dosya kontrol edecek.
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {//kanaldan geçen her veriyi durdurma yetkisine sahiptir.

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {//mesaj daha santrale (Broker)ya da kullanıcıya ulaşmadan "hemen önce" (preSend) araya girer.
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(//accessor değişkeninin içinde; stomp paketinin üzerindeki headeri okumak için paketi açar, paketin kime gittiği,tipi gibi bilgiler var
                message,
                StompHeaderAccessor.class
        );

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {//adece frontend ilk defa boruyu bağlamak istediğinde (CONNECT) kimlik sor, bağlantı kurulduktan sonra bir daha sorma.
            String authorization = accessor.getFirstNativeHeader("Authorization");//Tıpkı HTTP filtrende olduğu gibi, bağlantı isteğinin içindeki o Bearer ... yazan etiketi arar. Yoksa hata fırlatır

            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new MessagingException("Missing WebSocket authorization token.");
            }

            String token = authorization.substring(7);
            if (!jwtService.isTokenValid(token)) {//Token'ı alır, senin yazdığın JwtService  yollayıp "Bu geçerli mi?" diye sorar.
                throw new MessagingException("Invalid WebSocket authorization token.");
            }

            String userId = jwtService.extractUserId(token);//token içinden kullanıcının idsini alır websocket bağlantısının sahibi olarak kaydeder
            accessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, List.of()));//o an açık websocket bağlantısına UsernamePasswordAuthenticationToken oluşturup yapıştırır
        }

        return message;
    }
}
