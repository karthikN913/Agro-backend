package com.agrosystem.controller;

import com.agrosystem.model.DeliveryBid;
import com.agrosystem.model.Order;
import com.agrosystem.model.User;
import com.agrosystem.repository.DeliveryBidRepository;
import com.agrosystem.repository.OrderRepository;
import com.agrosystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/bids")
public class DeliveryBidController {

    @Autowired
    private DeliveryBidRepository deliveryBidRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/order/{orderId}")
    public List<DeliveryBid> getBidsForOrder(@PathVariable Long orderId) {
        return deliveryBidRepository.findByOrderId(orderId);
    }

    @GetMapping("/transporter/{transporterId}")
    public List<DeliveryBid> getBidsByTransporter(@PathVariable Long transporterId) {
        return deliveryBidRepository.findByTransporterId(transporterId);
    }

    @PostMapping
    public ResponseEntity<?> submitBid(@RequestBody Map<String, Object> body) {
        try {
            Long orderId = Long.valueOf(body.get("orderId").toString());
            Long transporterId = Long.valueOf(body.get("transporterId").toString());
            Double bidAmount = Double.valueOf(body.get("bidAmount").toString());
            String estimatedDeliveryTime = (String) body.get("estimatedDeliveryTime");

            Optional<Order> orderOpt = orderRepository.findById(orderId);
            Optional<User> transporterOpt = userRepository.findById(transporterId);

            if (orderOpt.isEmpty() || transporterOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Order or Transporter not found");
            }

            Order order = orderOpt.get();
            User transporter = transporterOpt.get();

            if (transporter.getRole() != User.Role.TRANSPORTER) {
                return ResponseEntity.badRequest().body("Only transporters can submit bids");
            }

            if (order.getStatus() != Order.Status.ACCEPTED) {
                return ResponseEntity.badRequest().body("Bidding is only allowed on ACCEPTED orders");
            }

            // Check if transporter already placed a bid
            List<DeliveryBid> existingBids = deliveryBidRepository.findByOrderId(orderId);
            boolean alreadyBidded = existingBids.stream()
                .anyMatch(b -> b.getTransporter().getId().equals(transporterId));
            if (alreadyBidded) {
                return ResponseEntity.badRequest().body("You have already placed a bid on this order");
            }

            DeliveryBid bid = new DeliveryBid();
            bid.setOrder(order);
            bid.setTransporter(transporter);
            bid.setBidAmount(bidAmount);
            bid.setEstimatedDeliveryTime(estimatedDeliveryTime);
            bid.setStatus(DeliveryBid.Status.PENDING);

            DeliveryBid saved = deliveryBidRepository.save(bid);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid request payload: " + e.getMessage());
        }
    }

    @PostMapping("/{bidId}/accept")
    public ResponseEntity<?> acceptBid(@PathVariable Long bidId) {
        Optional<DeliveryBid> bidOpt = deliveryBidRepository.findById(bidId);
        if (bidOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DeliveryBid acceptedBid = bidOpt.get();
        Order order = acceptedBid.getOrder();

        if (order.getStatus() != Order.Status.ACCEPTED) {
            return ResponseEntity.badRequest().body("Order is not available for bidding or has already been assigned");
        }

        // Accept this bid
        acceptedBid.setStatus(DeliveryBid.Status.ACCEPTED);
        deliveryBidRepository.save(acceptedBid);

        // Assign the transporter & delivery fee to the Order and transition status to SHIPPED
        order.setTransporter(acceptedBid.getTransporter());
        order.setDeliveryFee(acceptedBid.getBidAmount());
        order.setStatus(Order.Status.SHIPPED);
        orderRepository.save(order);

        // Reject all other bids for this order
        List<DeliveryBid> otherBids = deliveryBidRepository.findByOrderId(order.getId());
        for (DeliveryBid b : otherBids) {
            if (!b.getId().equals(bidId)) {
                b.setStatus(DeliveryBid.Status.REJECTED);
                deliveryBidRepository.save(b);
            }
        }

        return ResponseEntity.ok(order);
    }
}
