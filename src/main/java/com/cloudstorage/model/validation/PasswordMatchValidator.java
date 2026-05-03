package com.cloudstorage.model.validation;

import com.cloudstorage.model.dto.loginDTO.RegisterRequestDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch,RegisterRequestDTO>{
    @Override
    public boolean isValid(RegisterRequestDTO dto, ConstraintValidatorContext context) {
        if (dto.getPassword() == null || dto.getConfirmPassword() == null) return false;
        return dto.getPassword().equals(dto.getConfirmPassword());
    }
}
