package com.agrosystem.controller;

import com.agrosystem.model.Product;
import com.agrosystem.model.User;
import com.agrosystem.model.CropSubscription;
import com.agrosystem.model.Notification;
import com.agrosystem.repository.ProductRepository;
import com.agrosystem.repository.UserRepository;
import com.agrosystem.repository.CropSubscriptionRepository;
import com.agrosystem.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CropSubscriptionRepository cropSubscriptionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/debug")
    public ResponseEntity<?> debugDatabase() {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        try {
            long userCount = userRepository.count();
            long productCount = productRepository.count();
            return ResponseEntity.ok("DB Connection OK! Users count: " + userCount + ", Products count: " + productCount);
        } catch (Throwable t) {
            t.printStackTrace(pw);
            return ResponseEntity.status(500).body("DB Query Failed: " + t.getMessage() + "\n\nStacktrace:\n" + sw.toString());
        }
    }

    @GetMapping("/farmer/{farmerId}")
    public List<Product> getProductsByFarmer(@PathVariable Long farmerId) {
        return productRepository.findByFarmerId(farmerId);
    }

    /** GET /api/products/search — advanced search with filters */
    @GetMapping("/search")
    public List<Product> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String location) {
        return productRepository.searchProducts(
            query != null ? query.trim() : null,
            category != null ? category.trim() : null,
            minPrice,
            maxPrice,
            location != null ? location.trim() : null
        );
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> addProduct(@RequestBody Product product) {
        if (product.getFarmer() == null || product.getFarmer().getId() == null) {
            return ResponseEntity.badRequest().body("Farmer ID is required");
        }
        Optional<User> farmerOpt = userRepository.findById(product.getFarmer().getId());
        if (farmerOpt.isEmpty() || farmerOpt.get().getRole() != User.Role.FARMER) {
            return ResponseEntity.badRequest().body("Invalid Farmer");
        }
        product.setFarmer(farmerOpt.get());
        Product saved = productRepository.save(product);

        // Dispatch notifications to subscribers of this crop category
        try {
            List<CropSubscription> subs = cropSubscriptionRepository.findByCategory(saved.getCategory());
            List<Notification> notifications = new java.util.ArrayList<>();
            for (CropSubscription sub : subs) {
                // Don't notify the farmer themselves if they happen to be subscribed
                if (sub.getUser().getId().equals(saved.getFarmer().getId())) {
                    continue;
                }
                Notification notif = new Notification();
                notif.setUser(sub.getUser());
                notif.setTitle("🌾 New Alert: " + saved.getCategory() + " listed!");
                String loc = saved.getFarmer().getLocation() != null ? " in " + saved.getFarmer().getLocation() : "";
                notif.setMessage("Farmer " + saved.getFarmer().getName() + " listed " + saved.getName() + " for ₹" + saved.getPrice() + "/kg" + loc + ".");
                notifications.add(notif);
            }
            if (!notifications.isEmpty()) {
                notificationRepository.saveAll(notifications);
            }
        } catch (Exception e) {
            System.err.println("Error dispatching crop subscription notification: " + e.getMessage());
        }

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, @RequestParam Long farmerId) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Product product = productOpt.get();
        if (product.getFarmer() == null || !product.getFarmer().getId().equals(farmerId)) {
            return ResponseEntity.status(403).body("Unauthorized to delete this product");
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok("Product deleted successfully");
    }
}

