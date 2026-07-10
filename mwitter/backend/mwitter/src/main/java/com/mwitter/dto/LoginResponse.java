package com.mwitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class LoginResponse {

    private String id;

    private String username;

    private String email;

    private String token;//başarılı girişten sonra frontende gönderilen cevaba ekledik
}
