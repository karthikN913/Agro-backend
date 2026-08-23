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
        List<Object[]> result = reviewRepository.getReviewSummaryByProductId(productId);
        long count = 0;
        double average = 0.0;
        if (result != null && !result.isEmpty()) {
            Object[] row = result.get(0);
            if (row != null && row.length >= 2) {
                count = row[0] != null ? ((Number) row[0]).longValue() : 0;
                Double avgVal = row[1] != null ? ((Number) row[1]).doubleValue() : null;
                average = avgVal != null ? Math.round(avgVal * 10.0) / 10.0 : 0.0;
            }
        }
        return ResponseEntity.ok(Map.of(
            "count", count,
            "average", average
        ));
    }

    /** GET /api/reviews/summaries — get avg rating + count for all products in one request */
    @GetMapping("/summaries")
    public ResponseEntity<Map<String, Map<String, Object>>> getBatchReviewSummaries() {
        List<Object[]> results = reviewRepository.getBatchReviewSummaries();
        Map<String, Map<String, Object>> map = new java.util.HashMap<>();
        for (Object[] row : results) {
            if (row != null && row.length >= 3) {
                Long productId = (Long) row[0];
                long count = row[1] != null ? ((Number) row[1]).longValue() : 0;
                Double avgVal = row[2] != null ? ((Number) row[2]).doubleValue() : null;
                double average = avgVal != null ? Math.round(avgVal * 10.0) / 10.0 : 0.0;
                
                map.put(String.valueOf(productId), Map.of(
                    "count", count,
                    "average", average
                ));
            }
        }
        return ResponseEntity.ok(map);
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
