package com.busreservation.service.payment;

import java.util.UUID;

/**
 * UPI Strategy payment provider.
 */
public class UPIPayment implements PaymentProcessor {
    @Override
    public PaymentReceipt pay(double amount, String paymentDetails) {
        // Prototype mode: Accept any non-null UPI ID
        boolean success = paymentDetails != null && !paymentDetails.isBlank();
        String txId = "TXN-UPI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentReceipt(txId, amount, "UPI", success);
    }
}
