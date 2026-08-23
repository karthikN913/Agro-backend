package com.agrosystem.controller;

import com.agrosystem.model.User;
import com.agrosystem.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

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
            
            Optional<User> existingUserOpt = userRepository.findByFirebaseUid(uid);
            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();
                existingUser.setName(user.getName());
                existingUser.setPhone(user.getPhone());
                existingUser.setRole(user.getRole());
                existingUser.setLocation(user.getLocation());
                existingUser.setShopName(user.getShopName());
                User savedUser = userRepository.save(existingUser);
                return ResponseEntity.ok(savedUser);
            }
            
            // Pre-emptive unique constraint checks to avoid DB duplicate key exceptions
            if (email != null && !email.trim().isEmpty()) {
                Optional<User> userWithEmail = userRepository.findByEmail(email);
                if (userWithEmail.isPresent() && !userWithEmail.get().getFirebaseUid().equals(uid)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Email address is already in use by another account.");
                }
            }
            
            if (user.getPhone() != null && !user.getPhone().trim().isEmpty() && !user.getPhone().startsWith("N/A")) {
                Optional<User> userWithPhone = userRepository.findByPhone(user.getPhone());
                if (userWithPhone.isPresent() && !userWithPhone.get().getFirebaseUid().equals(uid)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Phone number is already in use by another account.");
                }
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
                User existingUser = userOpt.get();
                // Self-heal generic fallback names
                if ("Firebase User".equals(existingUser.getName()) && existingUser.getEmail() != null) {
                    String prefix = existingUser.getEmail().split("@")[0];
                    existingUser.setName(prefix);
                    userRepository.save(existingUser);
                }
                return ResponseEntity.ok(existingUser);
            }
            
            // Fallback: If user exists in Firebase but not locally (e.g. split-brain or Google Sign-In), auto-register them
            String fallbackEmail = decodedToken.getEmail() != null ? decodedToken.getEmail() : uid + "@placeholder.com";
            String fallbackName = decodedToken.getName() != null ? decodedToken.getName() : fallbackEmail.split("@")[0];
            
            // Check if user already exists locally by email
            Optional<User> userByEmailOpt = userRepository.findByEmail(fallbackEmail);
            if (userByEmailOpt.isPresent()) {
                User existingUser = userByEmailOpt.get();
                existingUser.setFirebaseUid(uid); // Link the firebaseUid
                if ("Firebase User".equals(existingUser.getName()) || existingUser.getName() == null || existingUser.getName().isEmpty() || existingUser.getName().startsWith("N/A")) {
                    existingUser.setName(fallbackName);
                }
                User savedUser = userRepository.save(existingUser);
                return ResponseEntity.ok(savedUser);
            }
            
            User newUser = new User();
            newUser.setFirebaseUid(uid);
            newUser.setEmail(fallbackEmail);
            newUser.setName(fallbackName);
            newUser.setRole(User.Role.FARMER); // Default fallback role
            newUser.setLocation("N/A");
            newUser.setPhone("N/A-" + uid); // Ensure perfect uniqueness and no string index errors
            
            User savedUser = userRepository.save(newUser);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        }
    }

    /** GET /api/users — all registered users (used by chat user list) */
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/vehicle")
    public ResponseEntity<?> updateVehicleProfile(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        if (user.getRole() != User.Role.TRANSPORTER) {
            return ResponseEntity.badRequest().body("Only transporters can register vehicle profiles");
        }
        
        user.setVehicleType((String) body.get("vehicleType"));
        user.setVehicleNumber((String) body.get("vehicleNumber"));
        if (body.get("vehicleCapacity") != null) {
            user.setVehicleCapacity(Double.valueOf(body.get("vehicleCapacity").toString()));
        }
        
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }
}
