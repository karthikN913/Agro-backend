package com.agrosystem.controller;

import com.agrosystem.model.Review;
import com.agrosystem.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    /** GET /api/reviews/product/{productId} — get all reviews for a product */
    @GetMapping("/product/{productId}")
    public List<Review> getReviewsByProduct(@PathVariable Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    /** GET /api/reviews/product/{productId}/summary — get avg rating + count */
    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<Map<String, Object>> getReviewSummary(@PathVariable Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        OptionalDouble avg = reviews.stream()
            .filter(r -> r.getRating() != null)
            .mapToInt(Review::getRating)
            .average();
        return ResponseEntity.ok(Map.of(
            "count", reviews.size(),
            "average", avg.isPresent() ? Math.round(avg.getAsDouble() * 10.0) / 10.0 : 0.0
        ));
    }

    /** POST /api/reviews — submit a new review (open to all buyers) */
    @PostMapping
    public ResponseEntity<Review> submitReview(@RequestBody Review review) {
        if (review.getReviewer() == null || review.getReviewer().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reviewRepository.save(review));
    }
}
