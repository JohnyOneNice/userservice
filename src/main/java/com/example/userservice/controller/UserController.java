package com.example.userservice.controller;

import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Хешируем пароль перед сохранением
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User createdUser = userRepository.save(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(userDetails.getUsername());
                    user.setFirstName(userDetails.getFirstName());
                    user.setLastName(userDetails.getLastName());
                    user.setEmail(userDetails.getEmail());
                    user.setPhone(userDetails.getPhone());
                    User updatedUser = userRepository.save(user);
                    return new ResponseEntity<>(updatedUser, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(
            @PathVariable String username,
            HttpServletRequest request,
            Authentication authentication) {
        String remoteAddr  = request.getRemoteAddr();
        // 1. Разрешаем доступ с внутренних IP
        if (remoteAddr.equals("127.0.0.1") ||
                remoteAddr.startsWith("172.") ||
                remoteAddr.startsWith("10.") ||
                remoteAddr.startsWith("192.168.")) {
            Optional<User> user = userRepository.findByUsername(username);
            return user.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        // 2. Проверяем, аутентифицирован ли пользователь (наличие JWT)
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String requesterName = authentication.getName();

        // 3. Только если имя из токена совпадает с запрошенным — разрешаем
        if (!username.equals(requesterName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: Access denied to user data");
        }

        return userRepository.findByUsername(username)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}