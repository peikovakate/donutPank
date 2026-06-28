package com.donutpank.bank.seed;

import com.donutpank.bank.currency.Currency;
import com.donutpank.bank.currency.CurrencyRepository;
import com.donutpank.bank.exchangerate.ExchangeRate;
import com.donutpank.bank.exchangerate.ExchangeRateRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(0)
@RequiredArgsConstructor
public class SampleDataPopulator implements ApplicationRunner {

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

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedCurrencies();
        seedExchangeRates();
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
}
