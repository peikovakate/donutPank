package com.donutpank.bank.transaction;

import com.donutpank.bank.account.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transactions_account_id", columnList = "account_id"),
        @Index(name = "idx_transactions_to_account_id", columnList = "to_account_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TransactionType type;

    /** For CREDIT/DEBIT, the account acted on; for EXCHANGE, the source account. */
    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** Only set when type is EXCHANGE — the destination account. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @NonNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TransactionStatus status;

    /** Set only when status is FAILED or REJECTED. */
    @Column(name = "reason_code", length = 32)
    private ReasonCode reasonCode;

    @Column(length = 255)
    private String description;

    @Column(name = "idempotency_key", unique = true, length = 64)
    private String idempotencyKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void markCompleted() {
        this.status = TransactionStatus.COMPLETED;
        this.reasonCode = null;
    }

    public void markFailed(ReasonCode reasonCode) {
        this.status = TransactionStatus.FAILED;
        this.reasonCode = reasonCode;
    }

    public void markRejected(ReasonCode reasonCode) {
        this.status = TransactionStatus.REJECTED;
        this.reasonCode = reasonCode;
    }
}
