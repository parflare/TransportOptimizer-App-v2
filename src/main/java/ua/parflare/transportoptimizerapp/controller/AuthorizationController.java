package ua.parflare.transportoptimizerapp.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ua.parflare.transportoptimizerapp.entity.User;
import ua.parflare.transportoptimizerapp.service.UserService;
import ua.parflare.transportoptimizerapp.service.impl.UserServiceImpl;

@Controller
@AllArgsConstructor
public class AuthorizationController {

    private final UserServiceImpl userService;
    private final AuthenticationProvider authenticationProvider;

    @GetMapping("/login")
    public String login() {
        return "login"; // Назва HTML файлу без розширення
    }

    @GetMapping("/register")
    public String register() {
        return "register"; // Назва HTML файлу без розширення
    }

    @GetMapping("/home")
    public String index() {
        return "index"; // Назва HTML файлу без розширення
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello"; // Назва HTML файлу без розширення
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login"; // перенаправляємо користувача на сторінку успішного виходу
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody User user) {
        try {
            userService.addUser(user);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody User user) {
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
            Authentication authentication = authenticationProvider.authenticate(authToken);
            if (authentication.isAuthenticated()) {
                return ResponseEntity.ok("User authenticated successfully");
            } else {
                return ResponseEntity.badRequest().body("Authentication failed");
            }
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }

}
