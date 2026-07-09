package com.mwitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    @NotBlank(message = "Post content cannot be empty")
    @Size(max = 200, message = "Post content cannot be longer than 200 characters")
    private String content;//kullanıcının yazdığı tweetin içeriği

    @NotBlank(message = "User id cannot be empty")
    private String userId;//tweetin hangi kullanıcı tarafından atıldığını bilmek için

}
