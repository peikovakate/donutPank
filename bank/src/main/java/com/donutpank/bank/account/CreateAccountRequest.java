package com.donutpank.bank.account;

import com.donutpank.bank.currency.CurrencyCode;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(@NotNull CurrencyCode currency) {
}
