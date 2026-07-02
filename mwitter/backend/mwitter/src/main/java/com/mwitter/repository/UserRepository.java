package com.mwitter.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mwitter.model.User;

public interface UserRepository extends MongoRepository<User, String> {

}
