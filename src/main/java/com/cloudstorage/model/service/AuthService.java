package com.cloudstorage.model.service;

import com.cloudstorage.model.dto.loginDTO.AuthResponseDTO;
import com.cloudstorage.model.dto.loginDTO.LoginRequestDTO;
import com.cloudstorage.model.dto.loginDTO.RegisterRequestDTO;
import com.cloudstorage.model.dto.loginDTO.RegisterResponseDTO;

public interface AuthService {
    RegisterResponseDTO saveUser(RegisterRequestDTO registerRequestDTO);
    AuthResponseDTO loginUser(LoginRequestDTO loginRequestDTO);
}
