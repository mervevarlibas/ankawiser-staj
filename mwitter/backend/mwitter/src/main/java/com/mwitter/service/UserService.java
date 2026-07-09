package com.mwitter.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.mwitter.dto.ProfileResponse;
import com.mwitter.dto.LoginRequest;
import com.mwitter.dto.LoginResponse; //userservice i spring e tanıtıyoruz
import com.mwitter.dto.ProfileResponse;
import com.mwitter.dto.UserResponse;
import com.mwitter.model.User;
import com.mwitter.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository; //bu sınıfın calısabilmesi için userrepository sınıfını kullanıyoruz. final ile değiştirilemez hale getiriyoruz.
private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public User saveUser(User user) { //kayıt edilen kullanıcıyı tekrar kullanabilmek için saveUser metodunu oluşturuyoruz
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {

            throw new RuntimeException("Email already exists.");//throw programın akışını durdurur ve bir hata mesajı döndürür. Burada, eğer kullanıcı zaten kayıtlıysa bir hata mesajı döndürülür.
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {

            throw new RuntimeException("Username already exists.");

        }
        String hashedPassword = passwordEncoder.encode(user.getPassword());

user.setPassword(hashedPassword);
        user.setRegistrationDate(LocalDateTime.now());//Kullanıcının kayıt tarihini o anki zaman yapıyor

        return userRepository.save(user);//user nesnesini mongoDB de kaydediyoruz ve kaydedilen kullanıcıyı geri döndürüyoruz

    }

    public LoginResponse login(LoginRequest loginRequest) {

        Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());//girilen email adresine sahip kullanıcı var mı
        if (user.isEmpty()) {

            throw new RuntimeException("Email or password is incorrect.");

        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword())) {//girilen şifre ile kayıtlı şifreyi karşılaştırıyoruz. matches metodu, girilen şifreyi hashleyip kayıtlı şifre ile karşılaştırır. eşleşmezse hata mesajı döndürüyoruz

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

    public void unfollowUser(String followerId, String followingId) {
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

    public List<UserResponse> getFollowing(String userId) {//kullanıcının takip ettiği kullanıcıları getirmek için getFollowing metodunu oluşturuyoruz

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        List<UserResponse> responses = new ArrayList<>();

        for (String followingId : user.getFollowing()) {//kullanıcının following listesindeki idleri tek tek geziyoruz

            User followingUser = userRepository.findById(followingId)
                    .orElseThrow(() -> new RuntimeException("User not found."));

            responses.add(new UserResponse(//bulduğumuz kullanıcıyı UserResponse DTO sınıfına çeviriyoruz ve responses listesine ekliyoruz
                    followingUser.getId(),
                    followingUser.getUsername(),
                    followingUser.getEmail()
            ));
        }

        return responses;
    }

    public List<UserResponse> getFollowers(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        List<UserResponse> responses = new ArrayList<>();

        for (String followerId : user.getFollowers()) {

            User followerUser = userRepository.findById(followerId)
                    .orElseThrow(() -> new RuntimeException("User not found."));

            responses.add(new UserResponse(
                    followerUser.getId(),
                    followerUser.getUsername(),
                    followerUser.getEmail()
            ));
        }

        return responses;
    }

    public ProfileResponse getProfile(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFollowers().size(),
                user.getFollowing().size()
        );
    }

}
