package com.mwitter.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reposts")//mongodb koleksiyon
@CompoundIndex(name = "user_post_unique", def = "{'userId': 1, 'postId': 1}", unique = true)//Bir userId ile bir postId ikilisi bu tabloda sadece bir kez yan yana gelebilir
public class Repost {

    @Id
    private String id;
    private String userId;
    private String postId;
    private LocalDateTime createdAt;
}
