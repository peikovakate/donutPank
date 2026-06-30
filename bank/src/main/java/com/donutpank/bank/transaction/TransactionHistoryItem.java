package com.donutpank.bank.transaction;

import com.donutpank.bank.ledger.LedgerEntry;
import com.donutpank.bank.paymentorder.PaymentOrderType;
import java.time.Instant;

public record TransactionHistoryItem(
        Long transactionId,
        PaymentOrderType type,
        String direction,
        String amount,
        String balanceAfter,
        String description,
        Instant createdAt) {

    public static TransactionHistoryItem from(LedgerEntry entry) {
        return new TransactionHistoryItem(
                entry.getPaymentOrder().getId(),
                entry.getPaymentOrder().getType(),
                entry.getDirection().name(),
                entry.getAmount().toPlainString(),
                entry.getBalanceAfter().toPlainString(),
                entry.getPaymentOrder().getDescription(),
                entry.getCreatedAt());
    }
}
