package ua.parflare.transportoptimizerapp.service;

import ua.parflare.transportoptimizerapp.entity.User;

import java.util.List;
import java.util.Optional;


public interface UserService {

    List<User> getAllUsers();

    Optional<User> getUserByName(String name);

    User addUser(User user);

}

