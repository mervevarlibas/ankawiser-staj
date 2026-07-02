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
@Document(collection = "users")

public class User {

@Id
    private String id;

    private String username;

    private String email;

    private String password;

    private String phoneNumber;

    private LocalDateTime createdAt;

}
