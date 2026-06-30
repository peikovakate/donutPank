package com.donutpank.bank.account;

import java.time.Instant;

public record AccountResponse(Long id, String currency, String balance, Instant createdAt) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCurrency().getCode(),
                account.getBalance().toPlainString(),
                account.getCreatedAt());
    }
}
