package com.mwitter.service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service; //userservice i spring e tanıtıyoruz

import com.mwitter.dto.LoginRequest;
import com.mwitter.dto.LoginResponse;
import com.mwitter.dto.UserResponse;
import com.mwitter.model.User;
import com.mwitter.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
private final UserRepository userRepository; //bu sınıfın calısabilmesi için userrepository sınıfını kullanıyoruz. final ile değiştirilemez hale getiriyoruz.

public User saveUser(User user) { //kayıt edilen kullanıcıyı tekrar kullanabilmek için saveUser metodunu oluşturuyoruz
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {

    throw new RuntimeException("Email already exists.");//throw programın akışını durdurur ve bir hata mesajı döndürür. Burada, eğer kullanıcı zaten kayıtlıysa bir hata mesajı döndürülür.
}
if (userRepository.findByUsername(user.getUsername()).isPresent()) {

    throw new RuntimeException("Username already exists.");

}
    user.setRegistrationDate(LocalDateTime.now());//Kullanıcının kayıt tarihini o anki zaman yapıyor

return userRepository.save(user);//user nesnesini mongoDB de kaydediyoruz ve kaydedilen kullanıcıyı geri döndürüyoruz

}
public LoginResponse login(LoginRequest loginRequest) {

    Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());//girilen email adresine sahip kullanıcı var mı
if (user.isEmpty()) {

    throw new RuntimeException("Email or password is incorrect.");

}
if (!user.get().getPassword().equals(loginRequest.getPassword())) {

    throw new RuntimeException("Email or password is incorrect.");

}
return new LoginResponse(user.get().getId(), user.get().getUsername(), user.get().getEmail());

}
public UserResponse findByUsername(String username) {//username e göre kullanıcıyı bulmak için findByUsername metodunu oluşturuyoruz

    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found."));//orElseThrow metodu, eğer kullanıcı bulunamazsa bir hata fırlatır.

    return new UserResponse(//return user deseydik kullanıcıya her şey giderdi. istediğimiz alanları göndermek için UserResponse DTO(Elindeki büyük veriden sadece gerekli kısmı karşı tarafa gönderiyor.) sınıfını kullanıyoruz.
            user.getId(),
            user.getUsername(),
            user.getEmail()
    );
}
public void followUser(String followerId, String followingId) {//void dedik çünkü  mevcut iki kullanıcıyı güncelleyeceğiz.geriye değer döndürmemiz gereken nesne yok
Optional<User> follower = userRepository.findById(followerId);//takip eden kullanıcıyı bulmak için findById metodunu kullanıyoruz
Optional<User> following = userRepository.findById(followingId);//takip edilen kullanıcıyı bulmak için findById metodunu kullanıyoruz
if (follower.isEmpty() || following.isEmpty()) {
    throw new RuntimeException("User not found.");
}
 if (followerId.equals(followingId)) {//string karşılaştırdığımız için equals metodunu kullanıyoruz. kullanıcı kendini takip edemez.
        throw new RuntimeException("You cannot follow yourself.");
    }
    User followerUser = follower.get();//opsiyonelden gerçek kullanıcıyı almak için get metodunu kullanıyoruz
    User followingUser = following.get();
    if (followerUser.getFollowing().contains(followingId)) {//contains metodu, bir listede belirli bir öğenin olup olmadığını kontrol eder. eğer takip eden kullanıcı zaten takip edilen kullanıcıyı takip ediyorsa hata mesajı döndürüyoruz
        throw new RuntimeException("You already follow this user.");
    }

    followerUser.getFollowing().add(followingId);//takip eden kullanıcıyı takip edilen kullanıcıya ekliyoruz
    followingUser.getFollowers().add(followerId);//takip edilen kullanıcıyı takip eden kullanıcıya ekliyoruz

    userRepository.save(followerUser);
    userRepository.save(followingUser);

}
public void unfollowUser(String followerId, String followingId){
Optional<User> follower = userRepository.findById(followerId);
Optional<User> following = userRepository.findById(followingId);
if (follower.isEmpty() || following.isEmpty()) {
        throw new RuntimeException("User not found.");
    }
if (followerId.equals(followingId)) {
        throw new RuntimeException("You cannot unfollow yourself.");
    }
 User followerUser = follower.get();
    User followingUser = following.get();

    if (!followerUser.getFollowing().contains(followingId)) {
        throw new RuntimeException("You are not following this user.");
    }

    followerUser.getFollowing().remove(followingId);
    followingUser.getFollowers().remove(followerId);

    userRepository.save(followerUser);
    userRepository.save(followingUser);

}

}
