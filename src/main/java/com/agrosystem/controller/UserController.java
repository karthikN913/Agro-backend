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

  @PostMapping("/firebase-login")
public ResponseEntity<?> firebaseLogin(@RequestBody Map<String, String> body) {
    return ResponseEntity.ok("TEST WORKING");
}

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
