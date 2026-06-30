package com.donutpank.bank.moneymovement;

import com.donutpank.bank.account.Account;
import com.donutpank.bank.paymentorder.PaymentOrder;
import com.donutpank.bank.paymentorder.PaymentOrderRepository;
import com.donutpank.bank.paymentorder.PaymentOrderStatus;
import com.donutpank.bank.paymentorder.PaymentOrderType;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentOrderAttemptRecorder {

    private final PaymentOrderRepository paymentOrderRepository;

    public PaymentOrderAttemptRecorder(PaymentOrderRepository paymentOrderRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
    }

    /**
     * Attempts to insert a PENDING payment order row. Runs in its own transaction so that a unique
     * constraint violation on {@code idempotencyKey} rolls back only this insert — not the caller's
     * transaction — leaving the caller free to catch the exception and fetch the existing row.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentOrder insert(PaymentOrderType type, Account account, Account toAccount,
                                BigDecimal amount, String idempotencyKey, String description) {
        return paymentOrderRepository.save(PaymentOrder.builder()
                .type(type)
                .account(account)
                .toAccount(toAccount)
                .amount(amount)
                .status(PaymentOrderStatus.PENDING)
                .description(description)
                .idempotencyKey(idempotencyKey)
                .build());
    }

    @Transactional(readOnly = true)
    public PaymentOrder findExisting(String idempotencyKey) {
        return paymentOrderRepository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new IllegalStateException(
                        "Idempotency key conflict but original payment order not found: " + idempotencyKey));
    }
}
