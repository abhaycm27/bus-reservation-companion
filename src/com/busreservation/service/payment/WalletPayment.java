package com.busreservation.service.payment;

import java.util.UUID;

/**
 * Mobile Wallet Strategy payment provider.
 */
public class WalletPayment implements PaymentProcessor {
    @Override
    public PaymentReceipt pay(double amount, String paymentDetails) {
        // Prototype mode: Accept any non-null wallet handle
        boolean success = paymentDetails != null && !paymentDetails.isBlank();
        String txId = "TXN-WALLET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentReceipt(txId, amount, "WALLET", success);
    }
}
