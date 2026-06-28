package com.donutpank.bank.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.donutpank.bank.currency.CurrencyRepository;
import com.donutpank.bank.exchangerate.ExchangeRate;
import com.donutpank.bank.exchangerate.ExchangeRateRepository;
import java.math.BigDecimal;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class SampleDataPopulatorTest {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Test
    void seedingTwiceProducesNoDuplicates() {
        SampleDataPopulator populator = new SampleDataPopulator(currencyRepository, exchangeRateRepository);

        populator.run(null);
        assertThat(currencyRepository.count()).isEqualTo(5);
        assertThat(exchangeRateRepository.count()).isEqualTo(20);

        populator.run(null);
        assertThat(currencyRepository.count()).isEqualTo(5);
        assertThat(exchangeRateRepository.count()).isEqualTo(20);
    }

    @Test
    void seededRatesAreReciprocalAcrossEveryPair() {
        SampleDataPopulator populator = new SampleDataPopulator(currencyRepository, exchangeRateRepository);
        populator.run(null);

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
}
