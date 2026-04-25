package com.agrosystem.controller;

import com.agrosystem.model.Product;
import com.agrosystem.model.User;
import com.agrosystem.repository.ProductRepository;
import com.agrosystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/farmer/{farmerId}")
    public List<Product> getProductsByFarmer(@PathVariable Long farmerId) {
        return productRepository.findByFarmerId(farmerId);
    }

    @PostMapping
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
        return ResponseEntity.ok(saved);
    }
}
