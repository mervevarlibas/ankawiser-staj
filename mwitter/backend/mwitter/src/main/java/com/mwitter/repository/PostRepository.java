package com.mwitter.repository;

import java.util.List;//hazır metodları kullanabilmek için MongoRepository sınıfını import ediyoruz.

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mwitter.model.Post;

public interface PostRepository extends MongoRepository<Post, String> {

    List<Post> findAllByOrderByCreatedAtDesc();//tüm postları oluşturulma tarihine göre azalan sırada listelemek 

    List<Post> findByUserIdOrderByCreatedAtDesc(String userId);

}
