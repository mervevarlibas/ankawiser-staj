package com.mwitter.model;//dosyanın hangi klasöre ait olduğunu belirtiyoruz 
//Spring Boot @SpringBootApplication sayesinde com.mwitter paketini tararken bu sınıfı da bulur.
import java.time.LocalDateTime;//saati ve tarihi birlikte tutar

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;//bu alan geçerli bir email adresi olmalıdır
import jakarta.validation.constraints.NotBlank;//bu alan boş olamaz
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
    @NotBlank(message = "Username cannot be empty")
     private String username;
    @Email(message = "Invalid email")//formatı geçerli bir email adresi olmalıdır
    @NotBlank(message = "Email cannot be empty")
     private String email;

    @NotBlank(message = "Password cannot be empty")
     private String password;//ilerde hashleyip saklayacağız

    @NotBlank(message = "Phone number cannot be empty")
     private String phoneNumber;

     private LocalDateTime registrationDate;

}
