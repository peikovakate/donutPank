package com.donutpank.bank.transaction;

import com.donutpank.bank.ledger.LedgerEntry;
import java.time.Instant;

public record LedgerEntryResponse(
        Long id,
        Long transactionId,
        Long accountId,
        String direction,
        String amount,
        String balanceAfter,
        Instant createdAt) {

    public static LedgerEntryResponse from(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getPaymentOrder().getId(),
                entry.getAccount().getId(),
                entry.getDirection().name(),
                entry.getAmount().toPlainString(),
                entry.getBalanceAfter().toPlainString(),
                entry.getCreatedAt());
    }
}
