package com.agrosystem.controller;

import com.agrosystem.model.User;
import com.agrosystem.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private FirebaseToken verifyToken(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Missing or invalid token");
        }
        String token = authHeader.replace("Bearer ", "");
        return FirebaseAuth.getInstance().verifyIdToken(token);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestHeader("Authorization") String authHeader, @RequestBody User user) {
        try {
            FirebaseToken decodedToken = verifyToken(authHeader);
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            
            if (userRepository.findByFirebaseUid(uid).isPresent()) {
                return ResponseEntity.badRequest().body("User already registered.");
            }
            
            user.setFirebaseUid(uid);
            user.setEmail(email);
            
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestHeader("Authorization") String authHeader) {
        try {
            FirebaseToken decodedToken = verifyToken(authHeader);
            String uid = decodedToken.getUid();
            
            Optional<User> userOpt = userRepository.findByFirebaseUid(uid);
            if (userOpt.isPresent()) {
                return ResponseEntity.ok(userOpt.get());
            }
            return ResponseEntity.status(404).body("User not found in local database.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
