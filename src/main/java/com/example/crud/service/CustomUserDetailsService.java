// src/main/java/com/example/demo/service/CustomUserDetailsService.java

package com.example.crud.service;

import com.example.crud.model.User;
import com.example.crud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Ищем пользователя по username или email
        User user = userRepo.findByUsername(usernameOrEmail).orElse(
                userRepo.findByEmail(usernameOrEmail).orElseThrow(
                        () -> new UsernameNotFoundException("User not found")
                )
        );

        // Преобразуем статус в GrantedAuthority
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(user.getStatus()))
        );
    }
}
