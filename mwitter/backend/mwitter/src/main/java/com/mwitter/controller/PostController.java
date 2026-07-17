package com.mwitter.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mwitter.dto.CreatePostRequest;
import com.mwitter.dto.MessageResponse;
import com.mwitter.dto.PostResponse;
import com.mwitter.service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController//bu sınıf http isteklerini karşılayacak bir controller olduğunu belirtiyoruz
@RequestMapping("/posts")//bu controllerin hangi url ile çağrılacağını belirtiyoruz
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;//çalışabilmesi için postservice i çağırıyoruz

    @PostMapping //(Adres: POST /posts) -> createPost
    public PostResponse createPost(@Valid @RequestBody CreatePostRequest request,
            Authentication authentication) {
        String userId = authentication.getName();

        return postService.createPost(request, userId);

    }

    @GetMapping
    public List<PostResponse> getAllPosts() {

        return postService.getAllPosts();

    }

    @GetMapping("/user/{userId}")
    public List<PostResponse> getPostsByUserId(@PathVariable String userId,Authentication authentication) {//@PathVariable ile url deki userId yi alıyoruz ve getPostsByUserId metoduna gönderiyoruz

        String currentUserId = authentication.getName();//arayüzde o profildeki postlar listelenirken, senin o postu önceden beğenip beğenmediğini bilmesi gerekiyor.Service katmanındaki convertToResponse metoduna bu iki ID'yi birden gönderiyor
    return postService.getPostsByUserId(userId, currentUserId);

    }
    @GetMapping("/{postId}") //tek bir postun üstüne tıklandığında sace  o postun verilerini yollamak için
public PostResponse getPostById(
        @PathVariable String postId,
        Authentication authentication) {

    String currentUserId = authentication.getName();
    return postService.getPostByIdForResponse(postId, currentUserId);
}

    @PostMapping("/{postId}/like")//postid urlden gelir,userid jwtden gelir
    public MessageResponse likePost(
            @PathVariable String postId, //urldeki  post idsini alır
            Authentication authentication) { //JWT filtresinin oluşturduğu kullanıcı bilgisini taşır.

        String userId = authentication.getName();//Tokenın içindeki kullanıcı id’sini alır.

        postService.likePost(postId, userId);

        return new MessageResponse(
                LocalDateTime.now(),
                "Post liked successfully."
        );
    }

    @PostMapping("/{postId}/unlike")
    public MessageResponse unlikePost(
            @PathVariable String postId,
            Authentication authentication) {

        String userId = authentication.getName();

        postService.unlikePost(postId, userId);// mantığı aynı sadece serviste remove yapar

        return new MessageResponse(//spring boot java nesnesini jsona dönüştürür
                LocalDateTime.now(),
                "Post unliked successfully."
        );
    }

    @PostMapping("/{postId}/repost")
    public MessageResponse repostPost(
            @PathVariable String postId,
            Authentication authentication) {

        postService.repostPost(postId, authentication.getName());

        return new MessageResponse(
                LocalDateTime.now(),
                "Post reposted successfully."
        );
    }

    @PostMapping("/{postId}/unrepost")
    public MessageResponse undoRepost(
            @PathVariable String postId,
            Authentication authentication) {

        postService.undoRepost(postId, authentication.getName());

        return new MessageResponse(
                LocalDateTime.now(),
                "Repost removed successfully."
        );
    }

    @DeleteMapping("/{postId}")
    public MessageResponse deletePost(
            @PathVariable String postId,
            Authentication authentication) {

        postService.deletePost(postId, authentication.getName());

        return new MessageResponse(
                LocalDateTime.now(),
                "Post deleted successfully."
        );
    }

    @GetMapping("/timeline")
public List<PostResponse> getTimeline(//sadece onun takip ettiği kişilerin gönderilerini postService.getTimeline üzerinden getirir.
        Authentication authentication) {//JWT filtresinin doğruladığı kullanıcı bilgisini taşır

    String userId = authentication.getName();//tokenin içindeki kullanıcı idsini alır

    return postService.getTimeline(userId);//kullanıcı idsini servise gönderir
}
}
