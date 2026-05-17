package com.agrosystem.controller;

import com.agrosystem.model.GovernmentScheme;
import com.agrosystem.repository.GovernmentSchemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schemes")
@CrossOrigin(origins = "*")
public class GovernmentSchemeController {

    @Autowired
    private GovernmentSchemeRepository governmentSchemeRepository;

    /** GET /api/schemes — returns all active schemes */
    @GetMapping
    public List<GovernmentScheme> getAllSchemes() {
        return governmentSchemeRepository.findAll();
    }

    /** POST /api/schemes — admin endpoint to add a new scheme */
    @PostMapping
    public ResponseEntity<GovernmentScheme> createScheme(@RequestBody GovernmentScheme scheme) {
        scheme.setActive(true);
        return ResponseEntity.ok(governmentSchemeRepository.save(scheme));
    }
}
