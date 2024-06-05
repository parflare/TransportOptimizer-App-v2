package ua.parflare.transportoptimizerapp.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.parflare.transportoptimizerapp.entity.User;
import ua.parflare.transportoptimizerapp.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/find/{name}")
    public ResponseEntity<Optional<User>> getUserByName(@PathVariable String name) {
        Optional<User> user = userService.getUserByName(name);
        return ResponseEntity.ok(user);
    }

}
