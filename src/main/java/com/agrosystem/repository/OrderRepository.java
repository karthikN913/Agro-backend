package com.agrosystem.repository;

import com.agrosystem.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerId(Long buyerId);
    List<Order> findByProduct_Farmer_Id(Long farmerId);
    List<Order> findByTransporterId(Long transporterId);
    List<Order> findByStatusAndTransporterIsNull(Order.Status status);
}
