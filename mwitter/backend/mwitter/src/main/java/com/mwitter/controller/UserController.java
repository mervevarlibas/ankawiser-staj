package com.mwitter.controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.mwitter.model.User;
import com.mwitter.service.UserService;
import com.mwitter.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import com.mwitter.dto.LoginResponse;
@RestController
@RequiredArgsConstructor

public class UserController {
private final UserService userService;

@PostMapping("/login")
public LoginResponse login(@RequestBody LoginRequest loginRequest) {

    return userService.login(loginRequest);

}
@PostMapping("/register")
public User registerUser(@Valid @RequestBody User user) {

    return userService.saveUser(user);

}

}
