package com.mwitter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailRequest {

    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "Verification code cannot be empty")
    private String code;
}