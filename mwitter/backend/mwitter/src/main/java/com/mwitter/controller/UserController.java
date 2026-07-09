package com.mwitter.controller;
//dışarıdan gelen isteklerin ilk karşılandığı yer
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.mwitter.dto.LoginRequest;
import com.mwitter.dto.LoginResponse;
import com.mwitter.model.User;
import com.mwitter.service.UserService;
import com.mwitter.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController//sınıfın HTTP isteklerini(get,post..) işleyebileceğini ve JSON formatında yanıtlar döndürebileceğini belirtir.
@RequiredArgsConstructor

public class UserController {
private final UserService userService; //çalışabilmesi için userservice i çağırıyoruz

@PostMapping("/login") //biri POST http://localhost:8080/login adresine bir istek gönderdiğinde bu metod çalışacak
public LoginResponse login(@RequestBody LoginRequest loginRequest) {

    return userService.login(loginRequest);//service gönderiyor.

}
@PostMapping("/register")//biri POST http://localhost:8080/register adresine bir istek gönderdiğinde bu metod çalışacak
public User registerUser(@Valid @RequestBody User user) {//valid user.java içindeki kurallara göre doğrulama yapıyor.requestbody Postman’dan gelen JSON’u Java nesnesine çevirir.

    return userService.saveUser(user);

}
@GetMapping("/users/search")
public UserResponse searchUser(@RequestParam String username) {//@RequestParam ile url deki username yi alıyoruz ve searchUser metoduna gönderiyoruz

    return userService.findByUsername(username);

}

}
