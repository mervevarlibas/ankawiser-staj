package com.mwitter.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {//bu sınıf frontend e post bilgilerini güvenli bir şekilde göndermek için. Veritabanına kaydedilmez sadece cevap oluşturmak için kullanılır.

    private String id;

    private String content;

    private LocalDateTime createdAt;

    private String username;

    private String userId;//kullanıcı adına basınca kullanıcıya ait postları getirebilmek için userId yi de göndermemiz gerekiyor.
}
