package com.cloudstorage.model.dto.loginDTO;

import java.time.LocalDateTime;

import com.cloudstorage.model.enums.AppRole;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RegisterResponseDTO {
    private String id;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AppRole role;
}
