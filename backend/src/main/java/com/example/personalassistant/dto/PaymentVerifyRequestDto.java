package com.example.personalassistant.dto;

import lombok.Data;

@Data
public class PaymentVerifyRequestDto {
    private Long bookingId;
    private String paymentId;
    private String orderId;
    private String signature;
    private String paymentMethod;
    private String upiVpa;
}
