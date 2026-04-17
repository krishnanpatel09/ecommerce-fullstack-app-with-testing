package com.TDD.Ecom.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private boolean success;
    private String transactionId;
    private String message;
} 