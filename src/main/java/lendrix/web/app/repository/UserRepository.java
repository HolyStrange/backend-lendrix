package lendrix.web.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import lendrix.web.app.entity.User;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    User findByUsernameIgnoreCase(String username);
}
