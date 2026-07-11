package com.mwitter.model;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment {
@Id
    private String id;//her yorumun benzersiz kimliği

    private String content;//yorumun yazısı

    private LocalDateTime createdAt;//üretildiği tarih

    private String userId;//hangi kullanıcının yazdığı

    private String postId;//hangi posta ait olduğu
}
