package com.busreservation.service.payment;

import java.time.Instant;

/**
 * Receipt detailing finished payment actions.
 */
public class PaymentReceipt {
    private final String transactionId;
    private final double amount;
    private final Instant timestamp;
    private final String method;
    private final boolean success;

    public PaymentReceipt(String transactionId, double amount, String method, boolean success) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.timestamp = Instant.now();
        this.method = method;
        this.success = success;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getMethod() {
        return method;
    }

    public boolean isSuccess() {
        return success;
    }
}
