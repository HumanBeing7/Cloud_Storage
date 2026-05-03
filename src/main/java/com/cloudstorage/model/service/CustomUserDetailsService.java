package com.cloudstorage.model.service;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cloudstorage.model.entity.Users;
import com.cloudstorage.model.repository.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService{
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //find the user from the email
        Users users = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Please Register now"));
        return new User(
                users.getEmail(),      // Spring will use this as the username
                users.getPassword(),   // Spring will use this BCrypt hash to check the math
                new ArrayList<>()     // This empty list is for Roles/Authorities (we'll add those later!)
        );
    }
    
}