package com.agrosystem.repository;

import com.agrosystem.model.CropSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CropSubscriptionRepository extends JpaRepository<CropSubscription, Long> {
    List<CropSubscription> findByUser_Id(Long userId);
    List<CropSubscription> findByCategory(String category);
    boolean existsByUser_IdAndCategory(Long userId, String category);
    void deleteByUser_IdAndCategory(Long userId, String category);
}
