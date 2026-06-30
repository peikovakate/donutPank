package com.donutpank.bank.moneymovement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreditRequest(
        @NotBlank String amount,
        @Size(max = 255) String description) {
}
