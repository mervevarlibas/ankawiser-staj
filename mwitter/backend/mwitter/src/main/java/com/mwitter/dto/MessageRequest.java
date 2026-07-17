package com.mwitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageRequest {

    @NotBlank(message = "Receiver is required.")
    private String receiverId;

    @NotBlank(message = "Message cannot be empty.")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters.")
    private String content;
}
