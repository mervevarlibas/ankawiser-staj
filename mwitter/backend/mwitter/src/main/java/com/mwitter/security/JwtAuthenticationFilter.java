package com.mwitter.security;//bu dosyanın görevi istekle gelen JWT tokenını okuyup kullanıcının gerçekten kim olduğunu Spring Security'ye söylemek.

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.mwitter.model.Role;
import com.mwitter.repository.UserRepository;

@Component// bu sınıfı otomatik oluştur hafızada bir nesne (bean) olarak hazır tut
public class JwtAuthenticationFilter extends OncePerRequestFilter {//Frontend'den gelen her bir HTTP isteği (GET, POST vs.) için bu kodu sadece ve sadece bir kez çalıştır

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(//bu metod her istekte otomatik çağrılır.istek sunucuya girmeden hemen arandığı yer
            HttpServletRequest request,//gelen istek
            HttpServletResponse response,//dönecek cevap
            FilterChain filterChain)//Sıradaki filtreye geç. veya controllera devam et
            throws ServletException, IOException {

        String authorizationHeader =//http isteğinin authorization header i getirir.
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {//tokenin beklediğimiz formatta olup olmadığını kontrol eder(Bearer TOKEN)

            filterChain.doFilter(request, response);
            return;//kimlik yok doğrulayamaz, içeri girer fakat kimliksiz
        }

        String token = authorizationHeader.substring(7);//bearer kısmını ayırır

        if (jwtService.isTokenValid(token)) {//Saf token'ı alır ve JwtService'teki o imza kontrol metoduna yollar.true ise aşağı gerçekleşir

            String userId = jwtService.extractUserId(token);//Bu artıklogin olan kullanıcınınid'si.

            userRepository.findById(userId).ifPresent(user -> {
                Role role = user.getRole() == null ? Role.USER : user.getRole();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))//Rolü yalnızca JWT’den değil, MongoDB’den tekrar okumamız bilinçli bir güvenlik kararıdır.
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);//bariyeri kaldırır ve onaylanmış, kimliği tespit edilmiş bu isteği hedefine (Controller'a) gönderir.
    }
}
