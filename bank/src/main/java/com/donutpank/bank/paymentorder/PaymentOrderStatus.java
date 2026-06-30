package com.donutpank.bank.paymentorder;

public enum PaymentOrderStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REJECTED;

    public boolean isTerminal() {
        return this != PENDING;
    }
}
