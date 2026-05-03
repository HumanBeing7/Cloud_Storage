package com.cloudstorage.model.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.cloudstorage.model.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {
    @Bean
    public AuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService, 
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        
        // 1. Give it the Translator
        authProvider.setUserDetailsService(userDetailsService);
        
        // 2. Give it the Math Checker
        authProvider.setPasswordEncoder(passwordEncoder);
        
        return authProvider;
    }

    //We need to explicitly add it for login and such
    @Bean //Interrogation Room
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF (Cross-Site Request Forgery). 
            // We disable this because we are building a Stateless REST API using JWTs, not browser sessions.
            .csrf(csrf -> csrf.disable()) 
            
            // 2. Configure Route Permissions
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // VIP List: Open the lobby doors!
                .requestMatchers("/api/auth/**", "/error").permitAll() //hahah it block even the delhivery error guy
                .anyRequest().authenticated()                // Lock down everything else
            );

        return http.build();
    }
}
