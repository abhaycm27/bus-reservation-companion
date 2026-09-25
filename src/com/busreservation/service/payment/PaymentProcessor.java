package com.busreservation.service.payment;

/**
 * Strategy pattern interface for dynamic payment systems.
 */
public interface PaymentProcessor {
    PaymentReceipt pay(double amount, String paymentDetails);
}
