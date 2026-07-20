package com.mwitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //Controller'ın token ve newPassword alanlarını okuyabilmesi için getter/setter üretir.
@NoArgsConstructor //Jackson'ın request JSON verisini DTO'ya çevirebilmesi için boş constructor üretir.
@AllArgsConstructor //Testlerde iki alanı birlikte vererek DTO üretmeyi kolaylaştırır.
public class ResetPasswordRequest {
    @NotBlank(message = "Reset token cannot be empty") //URL'den frontend'e, oradan backend'e gelen token'ın boş olmasını engeller.
    private String token; //Mail bağlantısındaki ham token'dır; UserService bunu hashleyip UserRepository'de arar.

    @NotBlank(message = "New password cannot be empty") //Yeni şifrenin boş veya yalnızca boşluk olmasını engeller.
    @Size(min = 6, message = "New password must be at least 6 characters") //Backend tarafında minimum şifre uzunluğunu zorunlu tutar.
    private String newPassword; //Reset sayfasından gelen ve BCryptPasswordEncoder ile hashlenerek User.password alanına yazılan şifredir.
}
