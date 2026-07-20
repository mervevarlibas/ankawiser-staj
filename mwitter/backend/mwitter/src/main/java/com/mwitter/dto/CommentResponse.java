package com.mwitter.dto; //Bu DTO frontend’e gönderilen yorum cevabıdır.
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
private String id;

    private String content;

    private LocalDateTime createdAt;

    private String postId;

    private String userId;

    private String username;//frontend yorum sahibinin adını göstereceği için username ekledim

    private List<MentionResponse> mentions;//içeriğinde var olan kullanıcı mentionlarının listesi frontend'e böyle gidecek
}
