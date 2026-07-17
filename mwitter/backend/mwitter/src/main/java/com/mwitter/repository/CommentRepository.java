package com.mwitter.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mwitter.model.Comment;

public interface CommentRepository extends MongoRepository<Comment, String> {//Comment modelimizi alıp MongoDB'ye bağlıyor

    void deleteByPostId(String postId);//PostService, yorumları silmek için o gün bu metodu çağırır

    List<Comment> findByPostIdOrderByCreatedAtAsc(String postId);//Verilen posta ait bütün yorumları oluşturulma tarihine göre eski → yeni sırala

}
