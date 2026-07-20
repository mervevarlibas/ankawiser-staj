package com.mwitter.controller;
//dışarıdan gelen isteklerin ilk karşılandığı yer

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mwitter.dto.ChangePasswordRequest;
import com.mwitter.dto.LoginRequest;
import com.mwitter.dto.LoginResponse;
import com.mwitter.dto.MessageResponse;
import com.mwitter.dto.ProfileResponse;
import com.mwitter.dto.RegisterRequest;
import com.mwitter.dto.ResendCodeRequest;
import com.mwitter.dto.UserResponse;
import com.mwitter.dto.VerifyEmailRequest;
import com.mwitter.dto.ForgotPasswordRequest;
import com.mwitter.dto.ResetPasswordRequest;
import com.mwitter.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController // sınıfın HTTP isteklerini(get,post..) işleyebileceğini ve JSON formatında yanıtlar döndürebileceğini belirtir.
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService; // çalışabilmesi için userservice i çağırıyoruz

    @PostMapping("/login") // biri POST http://localhost:8080/login adresine bir istek gönderdiğinde bu metod çalışacak
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {//kurallara uyup uymadığı,json formatındaki veriyi loginrequest içine koyar

        return userService.login(loginRequest);// service gönderiyor.

    }

    @PostMapping("/register") // biri POST http://localhost:8080/register adresine bir istek gönderdiğinde bu metod çalışacak
    public UserResponse registerUser(@Valid @RequestBody RegisterRequest request) {

        return userService.saveUser(request);

    }

    @GetMapping("/search")
    public List<UserResponse> searchUsers(@RequestParam String username) {//soru işaretinden sonraki username bilgisini koparıp alır ve userService.searchUsers("ali") şeklinde beyne iletir.

        return userService.searchUsers(username);
    }

    @PostMapping("/follow/{followingId}")
    public MessageResponse followUser(@PathVariable String followingId, // takip edilecek kişinin id si url den alınır
            Authentication authentication) {// JWT filtresinin doğruladığı kullanıcı bilgisini taşır.

        String followerId = authentication.getName();// token içinde takip işlemi yapan kullanıcının id sini getirtir

        userService.followUser(followerId, followingId);
        return new MessageResponse(LocalDateTime.now(), "User followed successfully.");
    }

    @PostMapping("/unfollow/{followingId}")
    public MessageResponse unfollowUser(@PathVariable String followingId,
            Authentication authentication) {
        String followerId = authentication.getName();
        userService.unfollowUser(followerId, followingId);
        return new MessageResponse(LocalDateTime.now(), "User unfollowed successfully.");
    }

    @GetMapping("/{userId}/following")
    public List<UserResponse> getFollowing(@PathVariable String userId) {

        return userService.getFollowing(userId);
    }

    @GetMapping("/{userId}/followers")
    public List<UserResponse> getFollowers(@PathVariable String userId) {

        return userService.getFollowers(userId);
    }

    @GetMapping("/{userId}")
    public ProfileResponse getProfile(@PathVariable String userId) {//urlden incelenecek kişinin idsini alır

        return userService.getProfile(userId);

    }

    @PostMapping("/verify-email")//arayüzde kullanıcı maile gelen kodu girip onaylaya bastığında buraya post isteği gelir
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {//VerifyEmailRequest kutusunun içindeki e-posta ve kod alınarak userService.verifyCode metoduna yollanır.

        userService.verifyCode(request.getEmail(), request.getCode());
        return new MessageResponse(LocalDateTime.now(), "Email verified successfully.");
    }

    @PostMapping("/resend-verification-code")
    public MessageResponse resendVerificationCode(@Valid @RequestBody ResendCodeRequest request) {

        userService.resendVerificationCode(request.getEmail());
        return new MessageResponse(LocalDateTime.now(), "Verification code resent.");
    }
    @PostMapping("/change-password")
public MessageResponse changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        Authentication authentication) {

    String userId = authentication.getName();
    userService.changePassword(userId, request);

    return new MessageResponse(LocalDateTime.now(), "Password changed successfully.");
}

    @PostMapping("/forgot-password") //Login modalından gelen isteği karşılar; SecurityConfig bu yolu JWT'siz açar.
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {//@Valid, DTO içindeki e-posta kurallarını service çağrılmadan çalıştırır.
        userService.requestPasswordReset(request.getEmail());//Doğrulanan email'i token üretme, kaydetme ve mail gönderme işlemleri için UserService'e yollar.
        return new MessageResponse(LocalDateTime.now(),
                "If the email is registered, a password reset link has been sent.");//Email var/yok ayrımı yapmadan aynı cevabı vererek hesapların keşfedilmesini önler.
    }

    @PostMapping("/reset-password") //Reset sayfasından gelen isteği karşılar; SecurityConfig bu yolu JWT'siz açar.
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {//Token boşluğu ve şifre uzunluğunu DTO anotasyonlarıyla doğrular.
        userService.resetPassword(request.getToken(), request.getNewPassword());//Token kontrolü ve BCrypt şifre güncellemesi için alanları UserService'e bağlar.
        return new MessageResponse(LocalDateTime.now(), "Password reset successfully.");//Frontend'in login sayfasına yönlendirme yapabilmesi için başarı cevabı döner.
    }
}
