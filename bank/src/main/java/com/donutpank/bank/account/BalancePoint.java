package com.donutpank.bank.account;

import java.time.Instant;

public record BalancePoint(Instant at, String balance) {
}
