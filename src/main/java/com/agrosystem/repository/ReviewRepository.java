package com.agrosystem.repository;

import com.agrosystem.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTargetUserId(Long targetUserId);
    List<Review> findByProductId(Long productId);
}
