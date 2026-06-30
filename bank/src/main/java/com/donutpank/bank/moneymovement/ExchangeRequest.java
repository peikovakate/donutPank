package com.donutpank.bank.moneymovement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExchangeRequest(
        @NotNull Long fromAccountId,
        @NotNull Long toAccountId,
        @NotBlank String fromAmount) {
}
