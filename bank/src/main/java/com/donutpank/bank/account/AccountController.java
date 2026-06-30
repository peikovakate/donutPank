package com.donutpank.bank.account;

import com.donutpank.bank.security.CurrentUser;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountResponse> listAccounts(@AuthenticationPrincipal CurrentUser currentUser) {
        return accountService.listAccounts(currentUser.id());
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@AuthenticationPrincipal CurrentUser currentUser,
                                                           @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(currentUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountId}")
    public AccountResponse getAccount(@AuthenticationPrincipal CurrentUser currentUser,
                                       @PathVariable Long accountId) {
        return accountService.getAccount(currentUser.id(), accountId);
    }

    @GetMapping("/{accountId}/balance-history")
    public List<BalancePoint> getBalanceHistory(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return accountService.getBalanceHistory(currentUser.id(), accountId, from, to);
    }
}
