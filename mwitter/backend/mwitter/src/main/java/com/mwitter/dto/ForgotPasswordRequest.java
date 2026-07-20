package com.mwitter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //Controller'ın request JSON içindeki email alanını okuyabilmesi için getter/setter üretir.
@NoArgsConstructor //Jackson'ın JSON verisini DTO'ya dönüştürürken kullanacağı boş constructor'ı üretir.
@AllArgsConstructor //Testlerde veya elle DTO oluştururken email alanını alan constructor'ı üretir.
public class ForgotPasswordRequest {
    @NotBlank(message = "Email cannot be empty") //POST /users/forgot-password isteğinde boş email gelmesini engeller.
    @Email(message = "Please enter a valid email address") //Girilen metnin e-posta biçiminde olmasını controller çalışmadan doğrular.
    private String email; //Login modalından gönderilen ve UserService'in UserRepository'de arayacağı e-posta adresidir.
}
