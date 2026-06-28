package com.donutpank.bank.exchangerate;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByBaseCurrencyCodeAndQuoteCurrencyCode(String baseCurrencyCode, String quoteCurrencyCode);

    List<ExchangeRate> findAllByOrderByBaseCurrencyCodeAscQuoteCurrencyCodeAsc();
}
