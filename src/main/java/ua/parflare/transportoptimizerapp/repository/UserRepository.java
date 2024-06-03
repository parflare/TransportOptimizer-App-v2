package ua.parflare.transportoptimizerapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import ua.parflare.transportoptimizerapp.entity.User;

import java.util.UUID;

@Repository
public interface UserRepository extends MongoRepository<User, UUID> {
    User findByName(String name);
}
