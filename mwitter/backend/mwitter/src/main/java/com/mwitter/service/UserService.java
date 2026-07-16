package com.mwitter.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.mwitter.dto.ChangePasswordRequest;
import com.mwitter.dto.LoginRequest;
import com.mwitter.dto.LoginResponse;
import com.mwitter.dto.ProfileResponse;
import com.mwitter.dto.RegisterRequest; //userservice i spring e tanıtıyoruz
import com.mwitter.dto.UserResponse;
import com.mwitter.model.User;
import com.mwitter.repository.UserRepository;
import com.mwitter.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final EmailService emailService;
    private final UserRepository userRepository; // bu sınıfın calısabilmesi için userrepository sınıfını kullanıyoruz.
                                                 // final ile değiştirilemez hale getiriyoruz.
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponse saveUser(RegisterRequest request) { // dışarıdan doğrudan user gelmiyor.kayıt için gerekli
                                                            // alanları taşıyan registerrequest geliyor
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {

            throw new RuntimeException("Email already exists.");// throw programın akışını durdurur ve bir hata mesajı
                                                                // döndürür. Burada, eğer kullanıcı zaten kayıtlıysa bir
                                                                // hata mesajı döndürülür.
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {

            throw new RuntimeException("Username already exists.");

        }

        User user = new User();// user vtabanına kaydolacak gerçek model,request veri taşıyıcı
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashedPassword);

        user.setRegistrationDate(LocalDateTime.now());// Kullanıcının kayıt tarihini o anki zaman yapıyor

        String code = generateVerificationCode(); // yeni doğrulama kodu üretilip kullanıcıya atanıyor
        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(2));
        user.setVerified(false);

        User savedUser = userRepository.save(user);// kayıt gene de vtabanına gidiyor ama doğrulanmamış bir şekilde
        emailService.sendVerificationMail(savedUser.getEmail(), code);
        return convertToResponse(savedUser);

    }

    public void verifyCode(String email, String code) {// doğrulama kodunu kontrol eder.

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.isVerified()) {
            throw new RuntimeException("Account already verified.");
        }
        if (user.getVerificationCodeExpiry() == null// süre doldu mu kontrolü
                || LocalDateTime.now().isAfter(user.getVerificationCodeExpiry())) {
            throw new RuntimeException("Verification code has expired. Please request a new one.");
        }

        if (!user.getVerificationCode().equals(code)) {
            throw new RuntimeException("Invalid verification code.");
        }
        user.setVerified(true);
        user.setVerificationCode(null); // kullanılan kod artık gereksiz, temizle
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);
    }

    private String generateVerificationCode() {// 6 haneli kod üretir
        int code = (int) (Math.random() * 900000) + 100000; // 100000-999999 arası
        return String.valueOf(code);
    }

    public void resendVerificationCode(String email) {// yeni kod

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.isVerified()) {
            throw new RuntimeException("Account already verified.");
        }

        String newCode = generateVerificationCode();
        user.setVerificationCode(newCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(2));

        userRepository.save(user);
        emailService.sendVerificationMail(user.getEmail(), newCode);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> // orelsethrow kullanıcı bulunmadığında zaten metodu durduruyor o yüzden
                                   // opsiyonel kısmını kaldırdım
                new RuntimeException("Email or password is incorrect."));

        if (!passwordEncoder.matches(
                loginRequest.getPassword(),
                user.getPassword())) {
            throw new RuntimeException("Email or password is incorrect.");
        }

        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your email before logging in.");
        }
        String token = jwtService.generateToken(user.getId());
        return convertToLoginResponse(user, token);// cevaba eklemek icin

    }

    public UserResponse findByUsername(String username) {// username e göre kullanıcıyı bulmak için findByUsername
                                                         // metodunu oluşturuyoruz

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found."));// orElseThrow metodu, eğer kullanıcı
                                                                            // bulunamazsa bir hata fırlatır.

        return convertToResponse(user);
    }

    public void followUser(String followerId, String followingId) {// void dedik çünkü mevcut iki kullanıcıyı
                                                                   // güncelleyeceğiz.geriye değer döndürmemiz gereken
                                                                   // nesne yok

        User followerUser = getUserById(followerId);
        User followingUser = getUserById(followingId);
        if (followerId.equals(followingId)) {// string karşılaştırdığımız için equals metodunu kullanıyoruz. kullanıcı
                                             // kendini takip edemez.
            throw new RuntimeException("You cannot follow yourself.");
        }

        if (followerUser.getFollowing().contains(followingId)) {// contains metodu, bir listede belirli bir ögenin olup
                                                                // olmadığını kontrol eder. eğer takip eden kullanıcı
                                                                // zaten takip edilen kullanıcıyı takip ediyorsa hata
                                                                // mesajı döndürüyoruz
            throw new RuntimeException("You already follow this user.");
        }

        followerUser.getFollowing().add(followingId);// takip eden kullanıcıyı takip edilen kullanıcıya ekliyoruz
        followingUser.getFollowers().add(followerId);// takip edilen kullanıcıyı takip eden kullanıcıya ekliyoruz

        userRepository.save(followerUser);
        userRepository.save(followingUser);

    }

    public void unfollowUser(String followerId, String followingId) {

        User followerUser = getUserById(followerId);
        User followingUser = getUserById(followingId);
        if (followerId.equals(followingId)) {
            throw new RuntimeException("You cannot unfollow yourself.");
        }

        if (!followerUser.getFollowing().contains(followingId)) {
            throw new RuntimeException("You are not following this user.");
        }

        followerUser.getFollowing().remove(followingId);
        followingUser.getFollowers().remove(followerId);

        userRepository.save(followerUser);
        userRepository.save(followingUser);

    }

    public List<UserResponse> getFollowing(String userId) {// kullanıcının takip ettiği kullanıcıları getirmek için
                                                           // getFollowing metodunu oluşturuyoruz
        User user = getUserById(userId);

        List<UserResponse> responses = new ArrayList<>();

        for (String followingId : user.getFollowing()) {// kullanıcının following listesindeki idleri tek tek geziyoruz
            User followingUser = getUserById(followingId);

            responses.add(convertToResponse(followingUser));
        }

        return responses;
    }

    public List<UserResponse> getFollowers(String userId) {
        User user = getUserById(userId);

        List<UserResponse> responses = new ArrayList<>();

        for (String followerId : user.getFollowers()) {
            User followerUser = getUserById(followerId);

            responses.add(convertToResponse(followerUser));
        }

        return responses;
    }

    public ProfileResponse getProfile(String userId) {
        User user = getUserById(userId);

        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFollowers().size(),
                user.getFollowing().size());
    }

    public User getUserById(String userId) {// hem userservice hem de commentservice kullanacağı için public.
                                            // kullanıcıyı id ile bulmak için getUserById metodunu oluşturuyoruz.User
                                            // nesnesi dönecek

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
    }

    private UserResponse convertToResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail());
    }

    private LoginResponse convertToLoginResponse(User user, String token) {
        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                token);
    }

    public List<UserResponse> searchUsers(String username) {

        List<User> users = userRepository.findByUsernameContainingIgnoreCase(username);

        List<UserResponse> responses = new ArrayList<>();

        for (User user : users) {
            responses.add(convertToResponse(user));
        }

        return responses;
    }
public void changePassword(String userId, ChangePasswordRequest request) {

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found."));

    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
        throw new RuntimeException("Old password is incorrect.");
    }

    String hashedPassword = passwordEncoder.encode(request.getNewPassword());
    user.setPassword(hashedPassword);

    userRepository.save(user);
}
}
