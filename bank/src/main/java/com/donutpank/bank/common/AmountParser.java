package com.donutpank.bank.common;

import java.math.BigDecimal;

public final class AmountParser {

    private AmountParser() {
    }

    public static BigDecimal parsePositiveAmount(String raw) {
        BigDecimal amount;
        try {
            amount = new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid amount: " + raw);
        }
        if (amount.scale() > 4) {
            throw new BadRequestException("Amount must not have more than 4 decimal places: " + raw);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero: " + raw);
        }
        return amount.setScale(4);
    }
}
