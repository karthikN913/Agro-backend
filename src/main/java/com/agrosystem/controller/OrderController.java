package com.agrosystem.controller;

import com.agrosystem.model.Order;
import com.agrosystem.model.Product;
import com.agrosystem.model.User;
import com.agrosystem.repository.OrderRepository;
import com.agrosystem.repository.ProductRepository;
import com.agrosystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

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

    @GetMapping("/transporter/{transporterId}")
    public List<Order> getOrdersByTransporter(@PathVariable Long transporterId) {
        return orderRepository.findByTransporterId(transporterId);
    }

    @GetMapping("/transporter/available")
    public List<Order> getAvailableDeliveries() {
        return orderRepository.findByStatusAndTransporterIsNull(Order.Status.ACCEPTED);
    }

    @PatchMapping("/{id}/assign/{transporterId}")
    public ResponseEntity<?> assignTransporter(@PathVariable Long id, @PathVariable Long transporterId) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        Optional<User> transporterOpt = userRepository.findById(transporterId);
        
        if (orderOpt.isEmpty() || transporterOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Order order = orderOpt.get();
        if (order.getTransporter() != null) {
            return ResponseEntity.badRequest().body("Order is already assigned to a transporter");
        }
        
        order.setTransporter(transporterOpt.get());
        order.setStatus(Order.Status.SHIPPED); // Automatically mark as shipped/in-transit when claimed
        orderRepository.save(order);
        
        return ResponseEntity.ok(order);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> placeOrder(@RequestBody Order order) {
        if (order.getBuyer() == null || order.getProduct() == null) {
            return ResponseEntity.badRequest().body("Buyer and Product are required");
        }

        Optional<User> buyerOpt = userRepository.findById(order.getBuyer().getId());
        Optional<Product> productOpt = productRepository.findById(order.getProduct().getId());

        if (buyerOpt.isEmpty() || productOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid Buyer or Product");
        }

        // Role validation: only BUYER or SHOP_OWNER can place orders
        User buyer = buyerOpt.get();
        if (buyer.getRole() == User.Role.TRANSPORTER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Transporters cannot place orders");
        }

        Product product = productOpt.get();
        if (product.getQuantity() < order.getQuantity()) {
            return ResponseEntity.badRequest().body("Not enough quantity available");
        }

        product.setQuantity(product.getQuantity() - order.getQuantity());
        productRepository.save(product);

        order.setBuyer(buyer);
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

    @PatchMapping("/{id}/location")
    public ResponseEntity<?> updateLocation(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Order order = orderOpt.get();
        String location = body.get("location");
        if (location == null) {
            return ResponseEntity.badRequest().body("Location is required");
        }
        order.setTransporterLocation(location);
        orderRepository.save(order);
        return ResponseEntity.ok(order);
    }
}
