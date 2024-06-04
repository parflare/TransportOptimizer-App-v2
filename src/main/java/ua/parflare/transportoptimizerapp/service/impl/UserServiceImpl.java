package ua.parflare.transportoptimizerapp.service.impl;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import ua.parflare.transportoptimizerapp.entity.User;
import ua.parflare.transportoptimizerapp.repository.UserRepository;
import ua.parflare.transportoptimizerapp.service.UserService;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByName(String name) {
        return userRepository.findByName(name);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public User addUser(@Valid User user) {
        if (userRepository.findByName(user.getName())!= null) {
            throw new IllegalArgumentException("Username already exists");
        }
        user.setId(UUID.randomUUID());
        user.setRole("USER");

        return userRepository.save(user);
    }


}
