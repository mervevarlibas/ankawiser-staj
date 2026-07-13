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

    private final CommentRepository commentRepository;//Yorumu MongoDB’ye kaydetmek ve yorumları çekmek için.

    private final UserService userService;//jwtden gelen userid ile kullanıcıyı bulacağız

    private final PostService postService;//yorum yapılacak postun var olup olmadığını öğrenmek için

    public CommentResponse createComment(
        CreateCommentRequest request, //bodyden gelir
        String postId, //urlden gelir
        String userId //jwtden gelir
) {

    User user = userService.getUserById(userId); //JWT’deki id gerçekten var mı kontrol edilir. ve username response için lazım

    Post post = postService.getPostById(postId); //Olmayan bir posta yorum yapılmasını engeller.

    Comment comment = new Comment(); //Boş yorum nesnesi oluşturulur.

    comment.setContent(request.getContent()); //Yorum metnini request’ten alır.

    comment.setCreatedAt(LocalDateTime.now()); //Yorum zamanını backend verir.

    comment.setUserId(user.getId()); //Yorum sahibini JWT kullanıcısı yapar.

    comment.setPostId(post.getId()); //Yorumu URL’deki posta bağlar.

    Comment savedComment = commentRepository.save(comment);//savedcomment mongodbye kaydedilmiş yorum

    return convertToResponse(savedComment, user.getUsername());//yorum yapanın kullanıcıadını alır

}
private CommentResponse convertToResponse(//bunları alıp frontend'in anlayacağı CommentResponse nesnesini oluşturuyor
        Comment comment,
        String username// ayrı geliyor çünkü comment içinde sadece useridvar
) {
// bu metod Comment + username = commentresponse yapar
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

    for (Comment comment : comments) {//yorumları tek tek gezer 

        User user = userService.getUserById(comment.getUserId());//her yorumun sahibini bulur 

        responses.add(
                convertToResponse(comment, user.getUsername())//dtoya çevirip listeye ekler
        );
    }

    return responses;
}
}