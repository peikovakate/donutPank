package com.donutpank.bank.exchangerate;

import static org.assertj.core.api.Assertions.assertThat;

import com.donutpank.bank.currency.Currency;
import com.donutpank.bank.currency.CurrencyRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class ExchangeRateRepositoryTest {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private Currency eur;
    private Currency usd;
    private Currency gbp;

    @BeforeEach
    void setUp() {
        eur = currencyRepository.save(
                Currency.builder().code("EUR").name("Euro").minorUnit((short) 2).active(true).build());
        usd = currencyRepository.save(
                Currency.builder().code("USD").name("US Dollar").minorUnit((short) 2).active(true).build());
        gbp = currencyRepository.save(
                Currency.builder().code("GBP").name("British Pound").minorUnit((short) 2).active(true).build());
    }

    @Test
    void findByBaseCurrencyCodeAndQuoteCurrencyCodeIsDirectionSpecific() {
        exchangeRateRepository.save(ExchangeRate.builder()
                .baseCurrency(eur)
                .quoteCurrency(usd)
                .rate(new BigDecimal("1.08000000"))
                .build());

        assertThat(exchangeRateRepository.findByBaseCurrencyCodeAndQuoteCurrencyCode("EUR", "USD")).isPresent();
        assertThat(exchangeRateRepository.findByBaseCurrencyCodeAndQuoteCurrencyCode("USD", "EUR")).isEmpty();
    }

    @Test
    void findAllByOrderByBaseCurrencyCodeAscQuoteCurrencyCodeAscIsSortedForDisplay() {
        exchangeRateRepository.save(ExchangeRate.builder()
                .baseCurrency(gbp)
                .quoteCurrency(usd)
                .rate(new BigDecimal("1.27000000"))
                .build());
        exchangeRateRepository.save(ExchangeRate.builder()
                .baseCurrency(eur)
                .quoteCurrency(usd)
                .rate(new BigDecimal("1.08000000"))
                .build());
        exchangeRateRepository.save(ExchangeRate.builder()
                .baseCurrency(eur)
                .quoteCurrency(gbp)
                .rate(new BigDecimal("0.86000000"))
                .build());

        List<ExchangeRate> rates = exchangeRateRepository.findAllByOrderByBaseCurrencyCodeAscQuoteCurrencyCodeAsc();

        assertThat(rates)
                .extracting(rate -> rate.getBaseCurrency().getCode() + "/" + rate.getQuoteCurrency().getCode())
                .containsExactly("EUR/GBP", "EUR/USD", "GBP/USD");
    }
}
