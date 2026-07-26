package com.mwitter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.mwitter.security.WebSocketAuthInterceptor;

import lombok.RequiredArgsConstructor;
//Bu dosya, sunucundaki anlık iletişim trafiğini yönetecek olan santral (Broker) ayarlarıdır.
@Configuration//bu ayar dosyası uygulama başlarken ilk burayı oku
@EnableWebSocketMessageBroker//Bu projede gerçek zamanlı anlık iletişim (WebSocket) olacak, mesaj yönlendirme motorunu (Broker) çalıştır
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {//stomp adresleme kurallarınn belirlendiği yer.frontend backend arası trafik bu yoldan akar
        registry.enableSimpleBroker("/queue");//Backend'den frontend'e gidecek mesajların çıkış kapısı.birine mesaj attığında sistem bunu /queue/messages adresine fırlatır
        registry.setApplicationDestinationPrefixes("/app");//Frontend'den backend'e gelecek mesajların giriş kapısı
        registry.setUserDestinationPrefix("/user");//canlı mesajlaşmada Sadece spesifik birine atacaksın. Bu satır sayesinde Spring arka planda her kullanıcıya özel gizli odalar oluşturur.mesajlar sadece ilgili kişinin odasına gönderilir
    }

    @Override//kanalın başlangıç noktası (endpoint)
    public void registerStompEndpoints(StompEndpointRegistry registry) {//frontend,backend ile canlı bağlantı kurmak istediğinde
        registry.addEndpoint("/ws")
                .withSockJS();//Eski bir tarayıcı WebSocket desteklemiyorsa bile sistemin çökmemesi için alternatif iletişim yolları üretir.
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {//gelen her mesaj paketinin araya bir güvenlik (webSocketAuthInterceptor) tarafından kesilip aranmasını sağlar
        registration.interceptors(webSocketAuthInterceptor);
    }
}
