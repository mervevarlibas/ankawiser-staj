package com.mwitter.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;// token imzalamak icin kullanılcan java nesnesi
    private final long expiration;// tokenın kaç milisaniye geçerli kalacağı 24saat yazdım propertieste

    public JwtService(
            @Value("${jwt.secret}") String secret, // application.properties içindeki jwt.secret=.. değerini alır
            @Value("${jwt.expiration}") long expiration) {// süre değerini alır

        this.secretKey = Keys.hmacShaKeyFor(// bu byte dizisinden imzasında kullanılabilecek secretkey oluşturur
                secret.getBytes(StandardCharsets.UTF_8)// gizli anahtar metnini byte dizisine çevirir. b anahtar sadece
                                                       // benim sunucumda var
        );

        this.expiration = expiration;
    }

    public String generateToken(String userId) {

        Date now = new Date();// tokenin üretildiği zamanı alır

        Date expirationDate = new Date(// şimdiki zamana geçerlilik süresi ekler
                now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId)// tokenin kime ait olduğunu belirtir
                .issuedAt(now)// tokenin üretildiği zamanı ekler
                .expiration(expirationDate)// tokenin geçerliliğinin biteceği zamanı ekler
                .signWith(secretKey)// tokeni gizli anahtarla imzalar
                .compact();// jwtyi üç parçalı metne dönüştürür
    }

    public String extractUserId(String token) {// kimlik okuma.istek geldiğinde

        return Jwts.parser()// jwtyi okuyacak bir parser oluşturmaya başlar
                .verifyWith(secretKey)// TOKEN imzasını bizim gizli anahtarımızla kontrol eder
                .build()// parser nesnesini hazırlar
                .parseSignedClaims(token)// gönderilen tokeni ayrıştırır ve imzasını doğrular
                .getPayload()
                .getSubject();// eğer imza bozulmamışsa içindeki kullanıcı ID'sini çıkarıp Controller'lardaki
                              // o Authentication nesnesinin içine koyulması için teslim eder.
    }

    public boolean isTokenValid(String token) {// token doğrulamayı dener

        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception exception) {

            return false;
        }
    }

}