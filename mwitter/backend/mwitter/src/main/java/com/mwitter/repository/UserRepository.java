package com.mwitter.repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import com.mwitter.model.User;

public interface UserRepository extends MongoRepository<User, String> {

Optional<User> findByEmail(String email);

Optional<User> findByUsername(String username);

}
