package com.donutpank.bank.moneymovement;

import com.donutpank.bank.security.CurrentUser;
import com.donutpank.bank.paymentorder.PaymentOrderResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class MoneyMovementController {

    private final MoneyMovementService moneyMovementService;

    public MoneyMovementController(MoneyMovementService moneyMovementService) {
        this.moneyMovementService = moneyMovementService;
    }

    @PostMapping("/accounts/{accountId}/credits")
    public ResponseEntity<PaymentOrderResponse> credit(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable Long accountId,
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 64) String idempotencyKey,
            @Valid @RequestBody CreditRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(moneyMovementService.credit(currentUser.id(), accountId, idempotencyKey, request));
    }

    @PostMapping("/accounts/{accountId}/debits")
    public ResponseEntity<PaymentOrderResponse> debit(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable Long accountId,
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 64) String idempotencyKey,
            @Valid @RequestBody DebitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(moneyMovementService.debit(currentUser.id(), accountId, idempotencyKey, request));
    }

    @PostMapping("/exchanges")
    public ResponseEntity<PaymentOrderResponse> exchange(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 64) String idempotencyKey,
            @Valid @RequestBody ExchangeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(moneyMovementService.exchange(currentUser.id(), idempotencyKey, request));
    }
}
