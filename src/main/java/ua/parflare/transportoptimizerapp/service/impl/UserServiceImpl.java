package ua.parflare.transportoptimizerapp.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.parflare.transportoptimizerapp.entity.User;
import ua.parflare.transportoptimizerapp.entity.enums.UserRole;
import ua.parflare.transportoptimizerapp.repository.UserRepository;
import ua.parflare.transportoptimizerapp.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByName(String name) {
        return userRepository.findByUserName(name);
    }

    public User addUser(User user) {
        String name = user.getUsername();
        if (userRepository.findByUserName(name).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User newUser = User.builder()
                .id(UUID.randomUUID())
                .userName(name)
                .password(passwordEncoder.encode(user.getPassword()))
                .email(user.getEmail())
                .active(true)
                .roles(Collections.singleton(UserRole.ROLE_USER))
                .build();

        return userRepository.save(newUser);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
