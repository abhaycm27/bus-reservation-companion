package com.busreservation.service.payment;

import java.util.UUID;

/**
 * Card Strategy payment provider.
 */
public class CardPayment implements PaymentProcessor {
    @Override
    public PaymentReceipt pay(double amount, String paymentDetails) {
        // Prototype mode: Accept any non-null card number
        boolean success = paymentDetails != null && !paymentDetails.isBlank();
        String txId = "TXN-CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentReceipt(txId, amount, "CARD", success);
    }
}
