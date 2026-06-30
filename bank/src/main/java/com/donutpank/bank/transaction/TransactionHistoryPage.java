package com.donutpank.bank.transaction;

import java.util.List;

public record TransactionHistoryPage(List<TransactionHistoryItem> items, String nextCursor) {
}
