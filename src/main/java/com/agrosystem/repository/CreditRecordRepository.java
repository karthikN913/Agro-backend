package com.agrosystem.repository;

import com.agrosystem.model.CreditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CreditRecordRepository extends JpaRepository<CreditRecord, Long> {
    List<CreditRecord> findByCreditor_IdOrderByCreatedAtDesc(Long creditorId);
    List<CreditRecord> findByCreditor_IdAndCustomerNameOrderByCreatedAtDesc(Long creditorId, String customerName);
}
