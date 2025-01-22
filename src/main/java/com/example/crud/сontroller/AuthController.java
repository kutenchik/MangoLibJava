// src/main/java/com/example/demo/controller/AuthController.java

package com.example.crud.сontroller;

import com.example.crud.service.FDATABASEService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final FDATABASEService dbService;
    private final BCryptPasswordEncoder encoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/registration")
    public String registrationPage() {
        return "reg";
    }

    @PostMapping("/registration")
    public String doRegister(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            Model model
    ) {
        // Проверяем формат логина/пароля
        if(!dbService.isValidInput(username) || !dbService.isValidInput(password)) {
            model.addAttribute("regError", "Пожалуйста, используйте только [a-zA-Z0-9_] для имени и пароля");
            return "reg";
        }
        // Хешируем
        String hash = encoder.encode(password);
        boolean res = dbService.addUser(username, hash, email);
        if (!res) {
            model.addAttribute("regError", "Пользователь с таким логином или почтой уже существует");
            return "reg";
        }
        return "redirect:/login";
    }
}
