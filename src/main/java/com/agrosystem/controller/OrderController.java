package com.agrosystem.controller;

import com.agrosystem.model.Order;
import com.agrosystem.model.Product;
import com.agrosystem.model.User;
import com.agrosystem.repository.OrderRepository;
import com.agrosystem.repository.ProductRepository;
import com.agrosystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/buyer/{buyerId}")
    public List<Order> getOrdersByBuyer(@PathVariable Long buyerId) {
        return orderRepository.findByBuyerId(buyerId);
    }

    @GetMapping("/farmer/{farmerId}")
    public List<Order> getOrdersByFarmer(@PathVariable Long farmerId) {
        return orderRepository.findByProduct_Farmer_Id(farmerId);
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody Order order) {
        if (order.getBuyer() == null || order.getProduct() == null) {
            return ResponseEntity.badRequest().body("Buyer and Product are required");
        }
        
        Optional<User> buyerOpt = userRepository.findById(order.getBuyer().getId());
        Optional<Product> productOpt = productRepository.findById(order.getProduct().getId());

        if (buyerOpt.isEmpty() || productOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid Buyer or Product");
        }

        Product product = productOpt.get();
        if (product.getQuantity() < order.getQuantity()) {
            return ResponseEntity.badRequest().body("Not enough quantity available");
        }

        product.setQuantity(product.getQuantity() - order.getQuantity());
        productRepository.save(product);

        order.setBuyer(buyerOpt.get());
        order.setProduct(product);
        order.setTotalPrice(product.getPrice() * order.getQuantity());
        order.setStatus(Order.Status.PENDING);
        order.setCreatedAt(java.time.LocalDateTime.now());

        Order saved = orderRepository.save(order);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Order order = orderOpt.get();
        try {
            Order.Status status = Order.Status.valueOf(body.get("status"));
            order.setStatus(status);
            orderRepository.save(order);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status");
        }
    }
}
