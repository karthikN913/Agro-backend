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

  @PostMapping("/firebase-login")
public ResponseEntity<?> firebaseLogin(@RequestBody Map<String, String> body) {
    try {
        String token = body.get("token");

        // Verify Firebase token
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);

        String email = decodedToken.getEmail();

        // Check if user exists in DB
        User user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not registered. Please register first.");
        }

        return ResponseEntity.ok(user);

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid Firebase token");
    }
}

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
