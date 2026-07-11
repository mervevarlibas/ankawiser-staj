package com.mwitter.dto;
import java.time.LocalDateTime;

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
}
