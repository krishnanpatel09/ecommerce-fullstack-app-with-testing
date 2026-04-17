package com.TDD.Ecom.service;

import com.TDD.Ecom.dto.PaymentRequest;
import com.TDD.Ecom.dto.PaymentResponse;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        try {
            logger.info("Processing payment for order: {}", paymentRequest.getOrderId());
            
            // Validate payment details
            if (!isValidCardNumber(paymentRequest.getCardNumber())) {
                throw new IllegalArgumentException("Invalid card number");
            }
            
            if (!isValidExpiryDate(paymentRequest.getExpiryDate())) {
                throw new IllegalArgumentException("Invalid expiry date");
            }
            
            if (!isValidCVV(paymentRequest.getCvv())) {
                throw new IllegalArgumentException("Invalid CVV");
            }

            // In a real application, this would call a payment gateway API
            // For now, we'll simulate a successful payment
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(true);
            response.setTransactionId(generateTransactionId());
            response.setMessage("Payment processed successfully");
            
            logger.info("Payment processed successfully for order: {}", paymentRequest.getOrderId());
            return response;
            
        } catch (Exception e) {
            logger.error("Payment processing failed: {}", e.getMessage());
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(false);
            response.setMessage("Payment failed: " + e.getMessage());
            return response;
        }
    }

    private boolean isValidCardNumber(String cardNumber) {
        // Basic validation - in real app, use proper card validation
        return cardNumber != null && cardNumber.matches("\\d{16}");
    }

    private boolean isValidExpiryDate(String expiryDate) {
        // Basic validation - in real app, use proper date validation
        return expiryDate != null && expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}");
    }

    private boolean isValidCVV(String cvv) {
        // Basic validation - in real app, use proper CVV validation
        return cvv != null && cvv.matches("\\d{3,4}");
    }

    private String generateTransactionId() {
        // Generate a random transaction ID
        return "TXN" + System.currentTimeMillis();
    }
} 