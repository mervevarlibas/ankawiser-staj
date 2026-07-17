package com.mwitter.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageResponse {

    private LocalDateTime timestamp;
    private String message;

    private String id;
    private String senderId;
    private String senderUsername;
    private String receiverId;
    private String receiverUsername;
    private String content;
    private LocalDateTime sentAt;
    private boolean read;

    public MessageResponse(LocalDateTime timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }
}
