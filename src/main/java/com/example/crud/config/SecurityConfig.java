// src/main/java/com/example/demo/config/SecurityConfig.java

package com.example.crud.config;

import com.example.crud.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
@RequiredArgsConstructor
public class SecurityConfig {
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/registration", "/poisk", "/random_manga", "/test",
                                "/css/**", "/js/**", "/img/**", "/static/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("Админ")
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .usernameParameter("username") // Оставляем "username", так как в форме name="username"
                        .passwordParameter("password") // Пароль остается "password"
                        .successHandler((request, response, authentication) -> {
                            Object savedRequest = request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");

                            if (savedRequest instanceof org.springframework.security.web.savedrequest.DefaultSavedRequest savedReq) {
                                try {
                                    response.sendRedirect(savedReq.getRedirectUrl());
                                } catch (Exception e) {
                                    response.sendRedirect("/"); // Если ошибка - редирект на главную
                                }
                            } else {
                                response.sendRedirect("/profile");
                            }
                        })
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );

        return http.build();
    }

// @Bean
//public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//    http
//            .csrf(csrf -> csrf.disable())
//            .authorizeHttpRequests(auth -> auth
//                    // Разрешаем доступ к любым URL без авторизации
//                    .anyRequest().permitAll()
//            )
//            // Можно оставить базовую конфигурацию формы логина (не будет требоваться)
//            .formLogin(Customizer.withDefaults())
//            // Или отключить, если форма логина не нужна
//            .logout(Customizer.withDefaults());
//
//    return http.build();
//}
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // DAO Authentication
    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider());
    }
}
