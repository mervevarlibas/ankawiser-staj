package com.mwitter.service;
import org.springframework.stereotype.Service;

import com.mwitter.model.User;
import com.mwitter.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
private final UserRepository userRepository;
public User saveUser(User user) {

    return userRepository.save(user);

}
}
