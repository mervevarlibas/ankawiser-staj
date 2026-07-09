package com.mwitter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mwitter.dto.CreatePostRequest;
import com.mwitter.dto.PostResponse;
import com.mwitter.service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController//bu sınıf http isteklerini karşılayacak bir controller olduğunu belirtiyoruz
@RequestMapping("/posts")//bu controllerin hangi url ile çağrılacağını belirtiyoruz
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;//çalışabilmesi için postservice i çağırıyoruz

    @PostMapping
    public PostResponse createPost(@Valid @RequestBody CreatePostRequest request) {

        return postService.createPost(request);//isteği postservice e gönderiyoruz ve postservice den dönen postu geri döndürüyoruz

    }

    @GetMapping
    public List<PostResponse> getAllPosts() {

        return postService.getAllPosts();

    }

    @GetMapping("/user/{userId}")
    public List<PostResponse> getPostsByUserId(@PathVariable String userId) {//@PathVariable ile url deki userId yi alıyoruz ve getPostsByUserId metoduna gönderiyoruz

        return postService.getPostsByUserId(userId);

    }
}
