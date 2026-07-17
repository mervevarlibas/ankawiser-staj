package com.mwitter.model;//dosyanın hangi klasöre ait olduğunu belirtiyoruz 
//Spring Boot @SpringBootApplication sayesinde com.mwitter paketini tararken bu sınıfı da bulur.
import java.time.LocalDateTime;//saati ve tarihi birlikte tutar
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;//MongoDB deki her belgenin benzersiz bir kimliğe sahip olduğunu belirtiyoruz
import org.springframework.data.mongodb.core.mapping.Document; //Bu sınıfın MongoDB'de bir collection olduğunu söyler.
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//lombok kütüphanesinin bir anotasyonu. Bu anotasyon, sınıfın tüm alanları için getter ve setter metodlarını otomatik olarak oluşturur. Ayrıca, equals(), hashCode() ve toString() metodlarını da oluşturur.
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")//bu sınıfın MongoDB de "users" koleksiyonuna karşılık geldiğini belirtiyoruz

public class User {

    @Id //MongoDB deki her belgenin benzersiz bir kimliğe sahip olduğunu belirtiyoruz
    private String id;
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private LocalDateTime registrationDate;
    private List<String> following = new ArrayList<>();//bu kullanıcının takip ettiği ve onu takip eden kişilerin sadece id numaralarını (String olarak) bir liste halinde tutuluyor
    private List<String> followers = new ArrayList<>();
    private boolean verified = false; //hesap doğrulandı mı. kullanıcı mail kodu doğru girene kadar false kalacak
    private String verificationCode; //kullanıcıya mailde gönderilen kod
    private LocalDateTime verificationCodeExpiry;//doğrulama kodunun süresi
}
