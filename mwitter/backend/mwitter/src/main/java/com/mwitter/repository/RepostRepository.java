package com.mwitter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mwitter.model.Repost;

public interface RepostRepository extends MongoRepository<Repost, String> {

    Optional<Repost> findByUserIdAndPostId(String userId, String postId);

    List<Repost> findByUserId(String userId);

    List<Repost> findByUserIdIn(List<String> userIds);

    long countByPostId(String postId);

    void deleteByPostId(String postId);
}
