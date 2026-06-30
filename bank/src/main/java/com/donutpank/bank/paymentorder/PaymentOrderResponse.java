package com.donutpank.bank.paymentorder;

import java.time.Instant;

public record PaymentOrderResponse(
        Long id,
        PaymentOrderType type,
        Long accountId,
        Long toAccountId,
        String amount,
        PaymentOrderStatus status,
        String reasonCode,
        String description,
        Instant updatedAt) {

    public static PaymentOrderResponse from(PaymentOrder paymentOrder) {
        return new PaymentOrderResponse(
                paymentOrder.getId(),
                paymentOrder.getType(),
                paymentOrder.getAccount().getId(),
                paymentOrder.getToAccount() != null ? paymentOrder.getToAccount().getId() : null,
                paymentOrder.getAmount().toPlainString(),
                paymentOrder.getStatus(),
                paymentOrder.getReasonCode() != null ? paymentOrder.getReasonCode().getCode() : null,
                paymentOrder.getDescription(),
                paymentOrder.getUpdatedAt());
    }
}
