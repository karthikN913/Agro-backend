package com.agrosystem.repository;

import com.agrosystem.model.CropSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CropSubscriptionRepository extends JpaRepository<CropSubscription, Long> {
    List<CropSubscription> findByUserId(Long userId);
    List<CropSubscription> findByCategory(String category);
    boolean existsByUserIdAndCategory(Long userId, String category);
    void deleteByUserIdAndCategory(Long userId, String category);
}
