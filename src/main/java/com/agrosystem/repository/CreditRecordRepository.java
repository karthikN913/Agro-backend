package com.agrosystem.repository;

import com.agrosystem.model.CreditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CreditRecordRepository extends JpaRepository<CreditRecord, Long> {
    List<CreditRecord> findByCreditorIdOrderByCreatedAtDesc(Long creditorId);
    List<CreditRecord> findByCreditorIdAndCustomerNameOrderByCreatedAtDesc(Long creditorId, String customerName);
}
