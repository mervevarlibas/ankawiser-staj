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
@Document(collection = "messages")//mongodbde bu şablonda üretilen her mesajı al koleksiyonda sakla
public class Message {

    @Id
    private String id;//her mesajın idsi

    private String senderId;//user sender gibi koca nesne tutmak yerine tek id tuttum
    private String receiverId;
    private String content;//gönderilen asıl metin
    private LocalDateTime sentAt;
    private boolean read = false;//mesaj üretildiğinde okunmamış,service katmanında true olacak
}