package com.mwitter.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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
    private String userId;//postun sahibi olan kullanıcı ID'si
    private Set<String> likedUserIds = new HashSet<>();//string beğenen kullanıcıların idsini tutar.set aynı değeri ikinci kez saklamaz. yani beğenen kullanıcı bir daha beğenemez.null değil boş olarak başyalacak.set olmasının sebbei aynı idyi iki kez tutmamakidyi iki kez tutmamak

}
