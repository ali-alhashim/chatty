package com.chatty.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http


                // Authorize access to specific endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/send-otp", "/otp", "/verify-otp", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )

                // Custom login page for unauthenticated users
                .formLogin(form -> form
                        .loginPage("/login") // Your custom login.html
                        .permitAll()
                )

                // Optional logout configuration
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                );

        return http.build();
    }
}