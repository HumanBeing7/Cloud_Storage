package com.cloudstorage.model.dto.loginDTO;

import com.cloudstorage.model.validation.PasswordMatch;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@PasswordMatch
public class RegisterRequestDTO {
    @NotBlank(message = "Email Cannot Be Empty")
    @Email(message = "Enter a valid Email Address")
    private String email;

    @NotBlank(message = "password can't be Empty")
    private String password;

    @NotBlank(message = "confirm pass can't be Empty")
    private String confirmPassword;
}
