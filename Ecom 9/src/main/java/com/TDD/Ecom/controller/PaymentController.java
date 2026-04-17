package com.TDD.Ecom.controller;

import com.TDD.Ecom.dto.PaymentRequest;
import com.TDD.Ecom.dto.PaymentResponse;
import com.TDD.Ecom.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PaymentController {
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            PaymentResponse response = paymentService.processPayment(paymentRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Payment processing failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
} 