package ua.parflare.transportoptimizerapp.service;

import org.springframework.http.ResponseEntity;
import ua.parflare.transportoptimizerapp.entity.User;

import java.util.List;


public interface UserService {

    public List<User> getAllUsers();

    public User getUserByName(String name);

    public User addUser(User user);

}

