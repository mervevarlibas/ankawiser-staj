package com.mwitter.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.mwitter.dto.MentionResponse;
import com.mwitter.model.User;
import com.mwitter.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MentionService {

    // @ işaretinden önce kullanıcı adı karakteri olmamalı. Böylece e-posta adresleri etiket sayılmaz.
    private static final Pattern MENTION_PATTERN =
            Pattern.compile("(?<![\\p{L}\\p{N}._])@([\\p{L}\\p{N}._]+)");//metin içindeki etiketleri bununla buluyor.@ etiketin başladığını belirtir.[\p{L}\p{N}._]+ kullanıcı adında harf, sayı, nokta ve alt çizgi kullanımına izin verir.test@example.com gibi e-posta adreslerindeki @ işareti etiket kabul edilmez.

    private final UserRepository userRepository;

    public List<MentionResponse> findValidMentions(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        Set<String> seenUsernames = new LinkedHashSet<>();
        List<MentionResponse> mentions = new ArrayList<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);

        while (matcher.find()) {
            String writtenUsername = matcher.group(1);
            String comparisonKey = writtenUsername.toLowerCase(Locale.ROOT);
            if (!seenUsernames.add(comparisonKey)) {//büyük/küçük harf duyarsız tekrar engelleme burada oluyor.
                continue;
            }

            userRepository.findByUsernameIgnoreCase(writtenUsername)//bulunan her kullanıcı adı mongodbded kontrol edilir
                    .map(this::toResponse)//MentionResponse'a çeviriyor
                    .ifPresent(mentions::add);//ile de listeye ekliyor
        }

        return mentions;
    }

    private MentionResponse toResponse(User user) {//kullanıcının id'si ve gerçek username'i alınıyor
        return new MentionResponse(user.getId(), user.getUsername());
    }
}
