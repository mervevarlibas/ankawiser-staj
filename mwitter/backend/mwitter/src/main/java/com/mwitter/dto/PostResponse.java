package com.mwitter.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private String id;

    private String content;

    private LocalDateTime createdAt;

    private String userId;

    private String username;

    private int likeCount;

    private boolean likedByCurrentUser;//ben begendim mi
}