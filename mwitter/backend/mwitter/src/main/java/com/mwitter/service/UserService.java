package com.mwitter.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor//final değişkenleri için otomatik bir kurucu oluşturur
public class UserService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();//Reset linki için Math.random yerine kriptografik olarak güçlü rastgele bayt üretir.

    private final EmailService emailService;//doğrulama mailleri atmak için
    private final UserRepository userRepository; // bu sınıfın calısabilmesi için userrepository sınıfını kullanıyoruz.
                                                 // final ile değiştirilemez hale getiriyoruz.
    private final BCryptPasswordEncoder passwordEncoder;//şifreleri karmaşık hale getirir güvenlik için
    private final JwtService jwtService;//giriş yapıldığında token basar
    private final NotificationService notificationService;//Başarılı follow işleminden sonra NotificationService üzerinden alıcıya bildirim üretir.

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public UserResponse saveUser(RegisterRequest request) { // dışarıdan doğrudan user gelmiyor.kayıt için gerekli
                                                            // alanları taşıyan registerrequest geliyor
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {

            throw new RuntimeException("Email already exists.");
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

    public void verifyCode(String email, String code) {// doğrulama kodunu kontrol eder.kullanıcı mailindeki kodu frontend e girdiğinde çalışır

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.isVerified()) {
            throw new RuntimeException("Account already verified.");
        }
        if (user.getVerificationCodeExpiry() == null// süre doldu mu kontrolü
                || LocalDateTime.now().isAfter(user.getVerificationCodeExpiry())) {
            throw new RuntimeException("Verification code has expired. Please request a new one.");
        }

        if (!user.getVerificationCode().equals(code)) {//kod doğru mu
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
                .orElseThrow(() -> // orelsethrow kullanıcı bulunmadığında zaten metodu durduruyor o yüzden opsiyonel kısmını kaldırdım
                new RuntimeException("Email or password is incorrect."));

        if (!passwordEncoder.matches(//kullanıcnın girdiği şifreyle veritabanındaki şifrelenmiş şifreyi kıyaslar
                loginRequest.getPassword(),
                user.getPassword())) {
            throw new RuntimeException("Email or password is incorrect.");
        }

        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your email before logging in.");
        }
        String token = jwtService.generateToken(user.getId());//jwt servis üzerinden bir token üretir ve bunu loginresponse olarak frontende gönderir
        return convertToLoginResponse(user, token);// cevaba eklemek icin

    }

    public UserResponse findByUsername(String username) {// username e göre kullanıcıyı bulmak için

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found."));// orElseThrow metodu, eğer kullanıcı bulunamazsa bir hata fırlatır.

        return convertToResponse(user);
    }

    public void followUser(String followerId, String followingId) {// void dedik çünkü mevcut iki kullanıcıyı güncelleyeceğiz.geriye değer döndürmemiz gereken nesne yok

        User followerUser = getUserById(followerId);
        User followingUser = getUserById(followingId);
        if (followerId.equals(followingId)) {// string karşılaştırdığımız için equals metodunu kullanıyoruz. kullanıcıkendini takip edemez.
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

        notificationService.notify(followingId, followerId, "FOLLOW", null);//Takip edileni alıcı, takip edeni aktör yapar; FOLLOW posta bağlı olmadığı için postId null gider.

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
                                                           
         User user = getUserById(userId);
    List<User> validUsers = getValidUsersAndCleanup(user, user.getFollowing(), true);//kontrol edilecek ana nesne, id listesi, bayrak

    List<UserResponse> responses = new ArrayList<>();//frontende göndermek için içi boş liste
    for (User followingUser : validUsers) {//validusers listesini tek tek dönüyor her adımda listeye atıyor
        responses.add(convertToResponse(followingUser));//kişiyi alıp şifresini vs gizlemesi için converttoresponse a alıyor sonra listeye ekliyor.
    }

    return responses;
    }

    public List<UserResponse> getFollowers(String userId) {
         User user = getUserById(userId);
    List<User> validUsers = getValidUsersAndCleanup(user, user.getFollowers(), false);

    List<UserResponse> responses = new ArrayList<>();
    for (User followerUser : validUsers) {
        responses.add(convertToResponse(followerUser));
    }

    return responses;
    }

    public ProfileResponse getProfile(String userId) {
         User user = getUserById(userId);

    int followersCount = getValidUsersAndCleanup(user, user.getFollowers(), false).size();
    int followingCount = getValidUsersAndCleanup(user, user.getFollowing(), true).size();

    return new ProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            followersCount,
            followingCount
    );
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
    user.setResetPasswordToken(null);//Profil içinden şifre değişince önceden alınmış reset linkini geçersiz kılar.
    user.setResetPasswordTokenExpiry(null);//İptal edilen reset linkinin son kullanım zamanını temizler.

    userRepository.save(user);
}

public void requestPasswordReset(String email) {//UserController'daki forgot-password endpoint'inden gelen e-posta için reset akışını başlatır.
    userRepository.findByEmail(email.trim()).ifPresent(user -> {//email veritabanında yoksa hiçbir şey olmuz, hata da fırlamaz=ifpresent
        byte[] randomBytes = new byte[32];//Tahmin edilmesi pratikte mümkün olmayan 256 bit token için boş bayt dizisi oluşturur.
        SECURE_RANDOM.nextBytes(randomBytes);//SecureRandom diziyi kriptografik olarak güçlü rastgele değerlerle doldurur.
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);//Baytları URL'de sorun çıkarmayan karakterlere dönüştürür.

        user.setResetPasswordToken(hashResetToken(rawToken));//Veritabanı sızsa bile link kullanılamasın diye ham token yerine yalnızca SHA-256 hashini saklar.
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(15));//Linkin backend tarafından kabul edileceği son zamanı 15 dakika sonrası yapar.
        userRepository.save(user);//Token özeti ve süreyi User koleksiyonunda kalıcı hale getirir.

        String resetLink = baseUrl + "/reset-password.html?token=" + rawToken;//linke hashlenmemiş token koyuyor.çünkü kullanıcı linke tıklayınca frontend bu ham token'ı backend'e gönderecek, backend onu tekrar hash'leyip veritabanındaki hash ile karşılaştıracak
        emailService.sendPasswordResetMail(user.getEmail(), resetLink);//Hazırlanan linki EmailService üzerinden kullanıcının kayıtlı adresine gönderir.
    });
}

public void resetPassword(String rawToken, String newPassword) {//ResetPasswordRequest içindeki token ve yeni şifreyi doğrulayıp User kaydını günceller.
    User user = userRepository.findByResetPasswordToken(hashResetToken(rawToken))
            .orElseThrow(() -> new RuntimeException("Invalid or expired reset link."));//Ham token tekrar hashlenir ve UserRepository üzerinden aynı hashe sahip kullanıcı aranır.

    if (user.getResetPasswordTokenExpiry() == null
            || !LocalDateTime.now().isBefore(user.getResetPasswordTokenExpiry())) {//Son kullanım zamanı yoksa veya geçmişse linki reddeder.
        user.setResetPasswordToken(null);//Süresi dolan token özetini temizleyerek tekrar sorgulanmasını engeller.
        user.setResetPasswordTokenExpiry(null);//Süresi dolan linkin tarih bilgisini User kaydından kaldırır.
        userRepository.save(user);//Süresi dolan token temizliğini MongoDB'ye kaydeder.
        throw new RuntimeException("Reset link has expired. Please request a new one.");//GlobalExceptionHandler üzerinden frontend'e kontrollü hata gönderir.
    }

    user.setPassword(passwordEncoder.encode(newPassword));//Yeni şifreyi düz metin saklamadan BCrypt ile hashleyip User.password alanına yazar.
    user.setResetPasswordToken(null);//Başarılı kullanımdan sonra token'ı tek kullanımlık hale getirmek için siler.
    user.setResetPasswordTokenExpiry(null);//Kullanılmış token'ın son kullanım zamanını temizler.
    userRepository.save(user);//Yeni şifreyi ve token temizliğini aynı User kaydında MongoDB'ye yazar.
}

private String hashResetToken(String rawToken) {//Mailde taşınan ham token'ı MongoDB'de saklanan sabit uzunluktaki özete dönüştürür.
    try {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(rawToken.getBytes(StandardCharsets.UTF_8));//Token metnini UTF-8 baytlarına çevirip SHA-256 ile tek yönlü hashler.
        return Base64.getEncoder().encodeToString(digest);//Hash baytlarını User.resetPasswordToken alanında saklanabilecek metne çevirir.
    } catch (NoSuchAlgorithmException exception) {
        throw new IllegalStateException("SHA-256 is not available.", exception);//JVM'de zorunlu algoritma yoksa uygulama yapılandırma hatasını bildirir.
    }
}
private List<User> getValidUsersAndCleanup(User owner, List<String> ids, boolean isFollowingList) {//silinmiş kullanıcıları takip listesinden temizleme

    List<User> foundUsers = userRepository.findAllById(ids);//mongodbden bu idlere sahip kullanıcıları getirtir

    // Bazı id'ler artık veritabanında yoksa (kullanıcı silinmiş),
    // listeyi sadece var olan id'lerle güncelleyip kalıcı olarak temizliyoruz.
    if (foundUsers.size() != ids.size()) {//diyelim üç id var ama birisi silindi 2 gözüküyor

        List<String> validIds = new ArrayList<>();//sadece var olan geçerli idleri tutmak için boş liste yaratıyor
        for (User u : foundUsers) {//sadece veritabanından dönmeyi başaran o 2 idyi çekiyor ve listeye ekliyor
            validIds.add(u.getId());
        }

        if (isFollowingList) {
            owner.setFollowing(validIds);
        } else {//metodu following değil de followers çağırdıysa bu listeyi temizliyor
            owner.setFollowers(validIds);
        }

        userRepository.save(owner);//gerçek hafızaya mongodbye atıyoruz
    }

    return foundUsers;
}
}
