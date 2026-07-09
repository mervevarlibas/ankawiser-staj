package com.mwitter.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;//Bu sınıfın MongoDB'de bir collection olduğunu söyler.

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts") //bu sınıfın MongoDB de "posts" koleksiyonuna karşılık geldiğini belirtiyoruz
public class Post {

    @Id//her postun benzersiz kimliği olacak
    private String id;
    private String content;
    private LocalDateTime createdAt;//postun oluşturulma tarihi
    private User user;//post nesnesinin içinde user nesnesi olacak !!!!!!!!BUNU userid ile düzeltecegim!!!!!!

}
