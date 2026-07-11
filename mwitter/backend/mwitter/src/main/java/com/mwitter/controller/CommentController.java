package com.mwitter.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mwitter.dto.CommentResponse;
import com.mwitter.dto.CreateCommentRequest;
import com.mwitter.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}/comments")
    public CommentResponse createComment(
            @PathVariable String postId,//URL’deki post id’sini Java değişkenine alır
            @Valid @RequestBody CreateCommentRequest request,//Body’de gelen JSON’u CreateCommentRequest nesnesine çevirir ve içindeki @NotBlank, @Size kurallarını çalıştırır.
            Authentication authentication) {//JWT filtresinin doğruladığı kullanıcı bilgisini taşı

        String userId = authentication.getName();//Yorumu yapan kullanıcının id’sini token’dan alır.

        return commentService.createComment(//alttaki üç bilgiyi servise yollar//alttaki üç bilgiyi servise yollar
                request,
                postId,
                userId
        );
    }

    @GetMapping("/{postId}/comments")
    public List<CommentResponse> getCommentsByPostId(
            @PathVariable String postId) {

        return commentService.getCommentsByPostId(postId);
    }
}
