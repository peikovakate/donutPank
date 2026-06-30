package com.donutpank.bank.transaction;

import com.donutpank.bank.security.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public TransactionHistoryPage listHistory(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable Long accountId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return transactionService.listHistory(currentUser.id(), accountId, cursor, limit);
    }
}
