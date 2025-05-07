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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@AllArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
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

    //  Открытый только для JWT-пользователей endpoint
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(
            @PathVariable String username,
            HttpServletRequest request,
            Authentication authentication) {

        String remoteAddr = request.getRemoteAddr();
        logger.info("Request from IP: {} to /username/{}", remoteAddr, username);

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
            logger.warn("Unauthorized access attempt to /username/{} from {}", username, remoteAddr);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String requesterName = authentication.getName();
        logger.info(" Authenticated username from token: {}", requesterName);

        // 3. Только если имя из токена совпадает с запрошенным — разрешаем
        if (!username.equals(requesterName)) {
            logger.warn("Forbidden: requested '{}' but token contains '{}'", username, requesterName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: Access denied to user data");
        }

        return userRepository.findByUsername(username)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Внутренняя точка — разрешена только из доверенных IP
    @GetMapping("/internal/username/{username}")
    public ResponseEntity<?> getUserByUsernameInternal(
            @PathVariable String username,
            HttpServletRequest request) {

        String remoteAddr = request.getRemoteAddr();
        logger.info("🛠 [INTERNAL] Incoming request from IP: {} for user {}", remoteAddr, username);

        // Разрешённые внутренние IP (например, IP пода authservice)
        if (
                remoteAddr.equals("10.244.1.131") ||   // authservice pod IP
                        remoteAddr.startsWith("10.") ||        // внутренняя подсеть
                        remoteAddr.startsWith("192.168.") ||
                        remoteAddr.startsWith("172.") ||
                        remoteAddr.equals("127.0.0.1")
        ) {
            return userRepository.findByUsername(username)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        logger.warn("[INTERNAL] Access denied for IP: {}", remoteAddr);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }

}