package com.mwitter.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    private String content;//kullanıcının yazdığı tweetin içeriği
    private String userId;//tweetin hangi kullanıcı tarafından atıldığını bilmek için

}
