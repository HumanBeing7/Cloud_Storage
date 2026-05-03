package com.cloudstorage.model.dto.loginDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor   
public class AuthResponseDTO { //200 -> OK(login) & 201 -> Created(Register)
    private String authToken;
}