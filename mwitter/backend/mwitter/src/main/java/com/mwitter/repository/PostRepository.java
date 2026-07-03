package com.mwitter.repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.mwitter.model.Post;
public interface PostRepository extends MongoRepository<Post, String> {

}
