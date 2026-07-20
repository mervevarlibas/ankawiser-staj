package com.mwitter.dto;

import java.time.LocalDateTime;
import java.util.List;

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

    private int repostCount;

    private boolean repostedByCurrentUser;

    private boolean repost;

    private String repostedByUserId;

    private String repostedByUsername;

    private LocalDateTime repostedAt;

    private List<MentionResponse> mentions;
}
