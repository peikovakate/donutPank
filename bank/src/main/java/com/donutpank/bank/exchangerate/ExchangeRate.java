package com.donutpank.bank.exchangerate;

import com.donutpank.bank.currency.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "exchange_rates", uniqueConstraints = {
        @UniqueConstraint(name = "uq_exchange_rates_pair", columnNames = {"base_currency", "quote_currency"})
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_currency", referencedColumnName = "code", nullable = false)
    private Currency baseCurrency;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_currency", referencedColumnName = "code", nullable = false)
    private Currency quoteCurrency;

    /** {@code 1 baseCurrency = rate * quoteCurrency}. */
    @NonNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal rate;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
