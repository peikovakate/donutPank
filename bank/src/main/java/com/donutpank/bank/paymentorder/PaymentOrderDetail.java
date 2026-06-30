package com.donutpank.bank.paymentorder;

import com.donutpank.bank.transaction.LedgerEntryResponse;
import java.time.Instant;
import java.util.List;

public record PaymentOrderDetail(
        Long id,
        PaymentOrderType type,
        Long accountId,
        Long toAccountId,
        String amount,
        PaymentOrderStatus status,
        String reasonCode,
        String description,
        Instant updatedAt,
        List<LedgerEntryResponse> ledgerEntries) {

    public static PaymentOrderDetail from(PaymentOrder paymentOrder, List<LedgerEntryResponse> ledgerEntries) {
        return new PaymentOrderDetail(
                paymentOrder.getId(),
                paymentOrder.getType(),
                paymentOrder.getAccount().getId(),
                paymentOrder.getToAccount() != null ? paymentOrder.getToAccount().getId() : null,
                paymentOrder.getAmount().toPlainString(),
                paymentOrder.getStatus(),
                paymentOrder.getReasonCode() != null ? paymentOrder.getReasonCode().getCode() : null,
                paymentOrder.getDescription(),
                paymentOrder.getUpdatedAt(),
                ledgerEntries);
    }
}
