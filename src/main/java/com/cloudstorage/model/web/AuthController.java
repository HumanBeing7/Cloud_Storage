package com.cloudstorage.model.web;

import org.springframework.web.bind.annotation.RestController;

import com.cloudstorage.model.dto.loginDTO.AuthResponseDTO;
import com.cloudstorage.model.dto.loginDTO.LoginRequestDTO;
import com.cloudstorage.model.dto.loginDTO.RegisterRequestDTO;
import com.cloudstorage.model.dto.loginDTO.RegisterResponseDTO;
import com.cloudstorage.model.service.AuthService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private AuthService authService;
    
    @PostMapping("/register")
    //do not send plain pass and email to frontend justlike that
    public ResponseEntity<RegisterResponseDTO> registerUser(@Valid @RequestBody RegisterRequestDTO request) {
        //take incoming request -> pass to service layer
        RegisterResponseDTO response = authService.saveUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO responseDTO = authService.loginUser(request);
        return ResponseEntity.ok().body(responseDTO);
    }
}
