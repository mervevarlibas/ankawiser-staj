package com.mwitter.controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mwitter.model.User;
import com.mwitter.service.UserService;

import lombok.RequiredArgsConstructor;
@RestController
@RequiredArgsConstructor

public class UserController {
private final UserService userService;

@PostMapping("/register")
public User registerUser(@RequestBody User user) {

    return userService.saveUser(user);

}

}
