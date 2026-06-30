package com.donutpank.bank.exchangerate;

import java.time.Instant;

public record ExchangeRateResponse(
        String baseCurrency,
        String quoteCurrency,
        String rate,
        Instant updatedAt) {

    public static ExchangeRateResponse from(ExchangeRate exchangeRate) {
        return new ExchangeRateResponse(
                exchangeRate.getBaseCurrency().getCode(),
                exchangeRate.getQuoteCurrency().getCode(),
                exchangeRate.getRate().toPlainString(),
                exchangeRate.getUpdatedAt());
    }
}
