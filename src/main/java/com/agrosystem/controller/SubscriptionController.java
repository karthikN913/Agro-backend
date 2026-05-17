package com.agrosystem.controller;

import com.agrosystem.model.CropSubscription;
import com.agrosystem.model.User;
import com.agrosystem.repository.CropSubscriptionRepository;
import com.agrosystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    @Autowired
    private CropSubscriptionRepository cropSubscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    /** GET /api/subscriptions?userId=... — list active crop subscriptions */
    @GetMapping
    public List<CropSubscription> getSubscriptions(@RequestParam Long userId) {
        return cropSubscriptionRepository.findByUserId(userId);
    }

    /** POST /api/subscriptions — subscribe to a crop category (idempotent) */
    @PostMapping
    public ResponseEntity<CropSubscription> subscribe(@RequestBody CropSubscription sub) {
        if (sub.getUser() == null || sub.getUser().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (sub.getCategory() == null || sub.getCategory().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userRepository.findById(sub.getUser().getId()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        sub.setUser(user);

        // Prevent duplicates
        boolean exists = cropSubscriptionRepository.existsByUserIdAndCategory(user.getId(), sub.getCategory());
        if (exists) {
            // Already subscribed, return OK
            return ResponseEntity.ok(sub);
        }

        return ResponseEntity.ok(cropSubscriptionRepository.save(sub));
    }

    /** DELETE /api/subscriptions — unsubscribe from a crop category */
    @DeleteMapping
    @Transactional
    public ResponseEntity<Void> unsubscribe(@RequestParam Long userId, @RequestParam String category) {
        cropSubscriptionRepository.deleteByUserIdAndCategory(userId, category);
        return ResponseEntity.ok().build();
    }
}
