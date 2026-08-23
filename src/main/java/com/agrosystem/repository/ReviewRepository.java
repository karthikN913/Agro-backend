package com.agrosystem.repository;

import com.agrosystem.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTargetUserId(Long targetUserId);
    List<Review> findByProductId(Long productId);

    @org.springframework.data.jpa.repository.Query("SELECT r.product.id, COUNT(r), AVG(r.rating) FROM Review r GROUP BY r.product.id")
    List<Object[]> getBatchReviewSummaries();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(r), AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    List<Object[]> getReviewSummaryByProductId(@org.springframework.data.repository.query.Param("productId") Long productId);
}
