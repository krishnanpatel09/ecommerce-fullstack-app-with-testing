package com.TDD.Ecom.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long orderId;
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
    private double amount;
} 