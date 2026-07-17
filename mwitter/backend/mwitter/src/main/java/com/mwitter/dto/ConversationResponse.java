package com.mwitter.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    private String userId;
    private String username;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private boolean lastMessageSentByCurrentUser;
    private long unreadCount;
}
