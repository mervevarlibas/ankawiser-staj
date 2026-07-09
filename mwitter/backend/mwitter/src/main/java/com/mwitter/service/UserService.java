package com.mwitter.service;
import java.time.LocalDateTime;
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
private final UserRepository userRepository; //final demek bu değişkenin sadece bir kez atanabileceğini ve değiştirilemeyeceğini belirtir.Bu servis vtabanı işlemleri için userropsitory kullanacak.
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
}
