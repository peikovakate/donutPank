package com.donutpank.bank.seed;

import com.donutpank.bank.account.Account;
import com.donutpank.bank.account.AccountRepository;
import com.donutpank.bank.currency.Currency;
import com.donutpank.bank.currency.CurrencyRepository;
import com.donutpank.bank.exchangerate.ExchangeRate;
import com.donutpank.bank.exchangerate.ExchangeRateRepository;
import com.donutpank.bank.ledger.Direction;
import com.donutpank.bank.ledger.LedgerEntry;
import com.donutpank.bank.ledger.LedgerRepository;
import com.donutpank.bank.paymentorder.PaymentOrder;
import com.donutpank.bank.paymentorder.PaymentOrderRepository;
import com.donutpank.bank.paymentorder.PaymentOrderStatus;
import com.donutpank.bank.paymentorder.PaymentOrderType;
import com.donutpank.bank.user.User;
import com.donutpank.bank.user.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(0)
@RequiredArgsConstructor
public class SampleDataPopulator implements ApplicationRunner {

    public static final String DEMO_USERNAME = "demo";
    public static final String DEMO_PASSWORD = "password";

    private static final Map<String, String> CURRENCY_NAMES = new LinkedHashMap<>();
    private static final Map<String, Integer> MINOR_UNITS = new LinkedHashMap<>();

    static {
        CURRENCY_NAMES.put("EUR", "Euro");
        CURRENCY_NAMES.put("USD", "US Dollar");
        CURRENCY_NAMES.put("SEK", "Swedish Krona");
        CURRENCY_NAMES.put("GBP", "British Pound");
        CURRENCY_NAMES.put("VND", "Vietnamese Dong");

        MINOR_UNITS.put("EUR", 2);
        MINOR_UNITS.put("USD", 2);
        MINOR_UNITS.put("SEK", 2);
        MINOR_UNITS.put("GBP", 2);
        MINOR_UNITS.put("VND", 0);
    }

    private static final Map<String, BigDecimal> USD_ANCHOR = Map.of(
            "USD", BigDecimal.ONE,
            "EUR", new BigDecimal("0.92"),
            "SEK", new BigDecimal("10.50"),
            "GBP", new BigDecimal("0.79"),
            "VND", new BigDecimal("25450"));

    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final LedgerRepository ledgerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedCurrencies();
        seedExchangeRates();
        seedDemoUser();
    }

    private void seedCurrencies() {
        CURRENCY_NAMES.forEach((code, name) -> currencyRepository.save(Currency.builder()
                .code(code)
                .name(name)
                .minorUnit(MINOR_UNITS.get(code).shortValue())
                .active(true)
                .build()));
    }

    private void seedExchangeRates() {
        for (String base : CURRENCY_NAMES.keySet()) {
            for (String quote : CURRENCY_NAMES.keySet()) {
                if (!base.equals(quote)) {
                    upsertRate(base, quote);
                }
            }
        }
    }

    private void upsertRate(String base, String quote) {
        BigDecimal rate = USD_ANCHOR.get(quote).divide(USD_ANCHOR.get(base), 8, RoundingMode.HALF_UP);
        Long existingId = exchangeRateRepository
                .findByBaseCurrencyCodeAndQuoteCurrencyCode(base, quote)
                .map(ExchangeRate::getId)
                .orElse(null);

        exchangeRateRepository.save(ExchangeRate.builder()
                .id(existingId)
                .baseCurrency(currencyRepository.getReferenceById(base))
                .quoteCurrency(currencyRepository.getReferenceById(quote))
                .rate(rate)
                .build());
    }

    /** No /auth/register endpoint exists, so seed a usable login plus a backdated history for the demo. */
    private void seedDemoUser() {
        if (userRepository.findByUsername(DEMO_USERNAME).isPresent()) {
            return;
        }

        User demo = userRepository.save(User.builder()
                .username(DEMO_USERNAME)
                .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                .build());

        Instant accountOpenedAt = Instant.now().minus(14, ChronoUnit.DAYS);
        Account eurAccount = openAccount(demo, "EUR", accountOpenedAt);
        Account usdAccount = openAccount(demo, "USD", accountOpenedAt);

        seedMovement(eurAccount, PaymentOrderType.CREDIT, new BigDecimal("1000.0000"),
                new BigDecimal("1000.0000"), "Initial deposit", daysAgo(10));
        seedMovement(eurAccount, PaymentOrderType.CREDIT, new BigDecimal("500.0000"),
                new BigDecimal("1500.0000"), "Salary", daysAgo(7));
        seedMovement(eurAccount, PaymentOrderType.DEBIT, new BigDecimal("300.2500"),
                new BigDecimal("1199.7500"), "Rent payment", daysAgo(4));
        seedMovement(eurAccount, PaymentOrderType.CREDIT, new BigDecimal("51.0000"),
                new BigDecimal("1250.7500"), "Refund", daysAgo(1));

        seedMovement(usdAccount, PaymentOrderType.CREDIT, new BigDecimal("300.0000"),
                new BigDecimal("300.0000"), "Initial deposit", daysAgo(6));
    }

    private Account openAccount(User owner, String currencyCode, Instant openedAt) {
        Account account = accountRepository.save(Account.builder()
                .user(owner)
                .currency(currencyRepository.getReferenceById(currencyCode))
                .balance(BigDecimal.ZERO.setScale(4))
                .build());
        backdate("accounts", account.getId(), "created_at", openedAt);
        return account;
    }

    private void seedMovement(Account account, PaymentOrderType type, BigDecimal amount,
                               BigDecimal balanceAfter, String description, Instant at) {
        PaymentOrder paymentOrder = paymentOrderRepository.save(PaymentOrder.builder()
                .type(type)
                .account(account)
                .amount(amount)
                .status(PaymentOrderStatus.COMPLETED)
                .description(description)
                .idempotencyKey(UUID.randomUUID().toString())
                .build());
        backdate("transactions", paymentOrder.getId(), "created_at", at);
        backdate("transactions", paymentOrder.getId(), "updated_at", at);

        Direction direction = type == PaymentOrderType.CREDIT ? Direction.CREDIT : Direction.DEBIT;
        LedgerEntry ledgerEntry = ledgerRepository.save(LedgerEntry.builder()
                .paymentOrder(paymentOrder)
                .account(account)
                .direction(direction)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .build());
        backdate("ledger", ledgerEntry.getId(), "created_at", at);

        if (type == PaymentOrderType.CREDIT) {
            account.credit(amount);
        } else {
            account.debit(amount);
        }
        accountRepository.save(account);
    }

    private Instant daysAgo(int days) {
        return Instant.now().minus(days, ChronoUnit.DAYS);
    }

    /**
     * {@code @CreationTimestamp}/{@code @UpdateTimestamp} fields always win over a pre-set value at
     * insert time, so backdating seed data requires a raw UPDATE after the initial save.
     */
    private void backdate(String table, Long id, String column, Instant at) {
        jdbcTemplate.update("UPDATE " + table + " SET " + column + " = ? WHERE id = ?", Timestamp.from(at), id);
    }
}
