package com.mwitter.service;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.mwitter.dto.LoginResponse;
import com.mwitter.model.User;
import com.mwitter.repository.UserRepository;
import com.mwitter.dto.LoginRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
private final UserRepository userRepository;
public User saveUser(User user) {
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {

    throw new RuntimeException("Email already exists.");
}
if (userRepository.findByUsername(user.getUsername()).isPresent()) {

    throw new RuntimeException("Username already exists.");

}
    user.setRegistrationDate(LocalDateTime.now());

return userRepository.save(user);

}
public LoginResponse login(LoginRequest loginRequest) {

    Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());
if (user.isEmpty()) {

    throw new RuntimeException("Email or password is incorrect.");

}
if (!user.get().getPassword().equals(loginRequest.getPassword())) {

    throw new RuntimeException("Email or password is incorrect.");

}
return new LoginResponse(user.get().getId(), user.get().getUsername(), user.get().getEmail());

}
}
