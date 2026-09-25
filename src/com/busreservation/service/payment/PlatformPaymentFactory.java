package com.busreservation.service.payment;

/**
 * Factory for creating PaymentProcessor Strategy instances.
 */
public class PlatformPaymentFactory {

    public static PaymentProcessor getPaymentProcessor(PaymentMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }
        return switch (method) {
            case UPI -> new UPIPayment();
            case CARD -> new CardPayment();
            case WALLET -> new WalletPayment();
        };
    }
}
