package com.agrosystem.controller;

import com.agrosystem.model.CreditRecord;
import com.agrosystem.model.User;
import com.agrosystem.repository.CreditRecordRepository;
import com.agrosystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/credit")
@CrossOrigin(origins = "*")
public class CreditController {

    @Autowired
    private CreditRecordRepository creditRecordRepository;

    @Autowired
    private UserRepository userRepository;

    /** GET /api/credit?userId=... — list all credit book records for a shop owner/farmer */
    @GetMapping
    public List<CreditRecord> getLedger(@RequestParam Long userId) {
        return creditRecordRepository.findByCreditorIdOrderByCreatedAtDesc(userId);
    }

    /** POST /api/credit — record a new credit or payback transaction */
    @PostMapping
    public ResponseEntity<CreditRecord> addRecord(@RequestBody CreditRecord record) {
        if (record.getCreditor() == null || record.getCreditor().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (record.getCustomerName() == null || record.getCustomerName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (record.getAmount() == null || record.getAmount() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        // Validate creditor existence
        User creditor = userRepository.findById(record.getCreditor().getId()).orElse(null);
        if (creditor == null) {
            return ResponseEntity.badRequest().build();
        }
        record.setCreditor(creditor);

        return ResponseEntity.ok(creditRecordRepository.save(record));
    }

    /** GET /api/credit/summary?userId=... — get outstanding debt summary and customer balances */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@RequestParam Long userId) {
        List<CreditRecord> records = creditRecordRepository.findByCreditorIdOrderByCreatedAtDesc(userId);

        double totalOutstanding = 0.0;
        double totalSettled = 0.0;

        // Map to group balance by customer phone/name
        Map<String, Map<String, Object>> customerMap = new LinkedHashMap<>();

        for (CreditRecord r : records) {
            String name = r.getCustomerName();
            String phone = r.getCustomerPhone() != null ? r.getCustomerPhone() : "N/A";
            double amount = r.getAmount();

            double change = r.getType() == CreditRecord.Type.CREDIT ? amount : -amount;

            if (r.getType() == CreditRecord.Type.CREDIT) {
                totalOutstanding += amount;
            } else {
                totalSettled += amount;
            }

            String key = name + "_" + phone;
            Map<String, Object> cData = customerMap.computeIfAbsent(key, k -> {
                Map<String, Object> m = new HashMap<>();
                m.put("name", name);
                m.put("phone", phone);
                m.put("outstanding", 0.0);
                return m;
            });

            double currentOutstanding = (double) cData.get("outstanding");
            cData.put("outstanding", currentOutstanding + change);
        }

        // Calculate actual net outstanding debt (credits extended minus repayments)
        double netOutstanding = totalOutstanding - totalSettled;

        return ResponseEntity.ok(Map.of(
            "totalCreditExtended", totalOutstanding,
            "totalRepayments", totalSettled,
            "netOutstanding", Math.max(0.0, netOutstanding),
            "customers", new ArrayList<>(customerMap.values())
        ));
    }
}
