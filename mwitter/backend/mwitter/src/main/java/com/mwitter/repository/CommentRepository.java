package com.mwitter.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mwitter.model.Comment;

public interface CommentRepository extends MongoRepository<Comment, String> {

    List<Comment> findByPostIdOrderByCreatedAtAsc(String postId);//Verilen posta ait bütün yorumları oluşturulma tarihine göre eski → yeni sırala

}