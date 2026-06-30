package com.donutpank.bank.seed;

import static org.assertj.core.api.Assertions.assertThat;


import com.donutpank.bank.account.AccountRepository;
import com.donutpank.bank.currency.CurrencyRepository;
import com.donutpank.bank.exchangerate.ExchangeRate;
import com.donutpank.bank.exchangerate.ExchangeRateRepository;
import com.donutpank.bank.ledger.LedgerRepository;
import com.donutpank.bank.paymentorder.PaymentOrderRepository;
import com.donutpank.bank.user.UserRepository;
import java.math.BigDecimal;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@DataJpaTest
class SampleDataPopulatorTest {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SampleDataPopulator populator() {
        return new SampleDataPopulator(
                currencyRepository, exchangeRateRepository,
                userRepository, accountRepository,
                paymentOrderRepository, ledgerRepository,
                new BCryptPasswordEncoder(), jdbcTemplate);
    }

    @Test
    void seedingTwiceProducesNoDuplicates() {
        SampleDataPopulator populator = populator();

        populator.run(null);
        assertThat(currencyRepository.count()).isEqualTo(5);
        assertThat(exchangeRateRepository.count()).isEqualTo(20);
        long userCount = userRepository.count();
        long accountCount = accountRepository.count();

        populator.run(null);
        assertThat(currencyRepository.count()).isEqualTo(5);
        assertThat(exchangeRateRepository.count()).isEqualTo(20);
        assertThat(userRepository.count()).isEqualTo(userCount);
        assertThat(accountRepository.count()).isEqualTo(accountCount);
    }

    @Test
    void seededRatesAreReciprocalAcrossEveryPair() {
        populator().run(null);

        for (ExchangeRate forward : exchangeRateRepository.findAll()) {
            BigDecimal backward = exchangeRateRepository
                    .findByBaseCurrencyCodeAndQuoteCurrencyCode(
                            forward.getQuoteCurrency().getCode(), forward.getBaseCurrency().getCode())
                    .orElseThrow()
                    .getRate();

            assertThat(forward.getRate().multiply(backward).doubleValue())
                    .isCloseTo(1.0, Offset.offset(0.0001));
        }
    }

    @Test
    void seedsDemoUserWithAccountsAndHistory() {
        populator().run(null);

        assertThat(userRepository.findByUsername(SampleDataPopulator.DEMO_USERNAME)).isPresent();
        assertThat(accountRepository.findByUserIdOrderByIdAsc(
                userRepository.findByUsername(SampleDataPopulator.DEMO_USERNAME).orElseThrow().getId()))
                .hasSize(2);
        assertThat(paymentOrderRepository.count()).isEqualTo(5);
        assertThat(ledgerRepository.count()).isEqualTo(5);
    }
}
