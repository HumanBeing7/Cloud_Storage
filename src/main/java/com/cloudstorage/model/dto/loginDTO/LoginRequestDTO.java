package com.cloudstorage.model.dto.loginDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @Email(message = "Enter a Valid Email")
    @NotBlank(message = "This Field Cannot be Empty")
    private String email;

    @NotBlank(message = "We do need Password!!")    
    private String password;
}
