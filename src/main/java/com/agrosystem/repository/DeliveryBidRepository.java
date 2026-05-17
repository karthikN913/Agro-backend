package com.agrosystem.repository;

import com.agrosystem.model.DeliveryBid;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryBidRepository extends JpaRepository<DeliveryBid, Long> {
    List<DeliveryBid> findByOrderId(Long orderId);
    List<DeliveryBid> findByTransporterId(Long transporterId);
}
