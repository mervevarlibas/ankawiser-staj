package com.mwitter.service;

import java.time.LocalDateTime;
import com.mwitter.model.Comment;
import org.springframework.stereotype.Service;
import com.mwitter.model.User;
import com.mwitter.dto.CommentResponse;
import com.mwitter.dto.CreateCommentRequest;
import com.mwitter.model.Post;
import com.mwitter.repository.CommentRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final UserService userService;//jwtden gelen userid ile kullanıcıyı bulacağız

    private final PostService postService;//yorum yapılacak postun var olup olmadığını öğrenmek için

    public CommentResponse createComment(
        CreateCommentRequest request,
        String postId,
        String userId
) {

    User user = userService.getUserById(userId);

    Post post = postService.getPostById(postId);

    Comment comment = new Comment();

    comment.setContent(request.getContent());

    comment.setCreatedAt(LocalDateTime.now());

    comment.setUserId(user.getId());

    comment.setPostId(post.getId());

    Comment savedComment = commentRepository.save(comment);//savedcomment mongodbye kaydedilmiş yorum

    return convertToResponse(savedComment, user.getUsername());//yorum yapanın kullanıcıadını alır

}
private CommentResponse convertToResponse(//bunları alıp frontend'in anlayacağı CommentResponse nesnesini oluşturuyor
        Comment comment,
        String username
) {

    return new CommentResponse(
            comment.getId(),
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getPostId(),
            comment.getUserId(),
            username
    );
}
public List<CommentResponse> getCommentsByPostId(String postId) {

    postService.getPostById(postId);//Önce post gerçekten var mı diye kontrol ediyor. Yoksa Post not found. hatası verir

    List<Comment> comments =
            commentRepository.findByPostIdOrderByCreatedAtAsc(postId);//Bu posta ait yorumları eskiden yeniye doğru getirir

    List<CommentResponse> responses = new ArrayList<>();//Frontend’e döndüreceğimiz boş listeyi oluşturur.

    for (Comment comment : comments) {//yorumları tek tek gezer//yorumları tek tek gezer

        User user = userService.getUserById(comment.getUserId());//her yorumun sahibini bulur//her yorumun sahibini bulur

        responses.add(
                convertToResponse(comment, user.getUsername())//dtoya çevirip listeye ekler//dtoya çevirip listeye ekler
        );
    }

    return responses;
}
}