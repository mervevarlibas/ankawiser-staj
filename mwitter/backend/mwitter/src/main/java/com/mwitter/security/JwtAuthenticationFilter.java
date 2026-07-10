package com.mwitter.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =//http isteğinin authorization başlığını getirir.
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {//tokenin beklediğimiz formatta olup olmadığını kontrol eder(Bearer TOKEN)

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);//bearer kısmını ayırır

        if (jwtService.isTokenValid(token)) {

            String userId = jwtService.extractUserId(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(//kimlik oluşturma. userid giriş yapan kullanıcının kimliği,null şifreyi almıyoruz,yetki listesi(yok daha)
                            userId,
                            null,
                            List.of()
                    );

            SecurityContextHolder//bu isteği yapan kullanıcı doğrulandı ve kimliği bu > userId
                    .getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}