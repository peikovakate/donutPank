package com.donutpank.bank.exchangerate;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateController(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @GetMapping
    public List<ExchangeRateResponse> listRates() {
        return exchangeRateRepository.findAllByOrderByBaseCurrencyCodeAscQuoteCurrencyCodeAsc()
                .stream()
                .map(ExchangeRateResponse::from)
                .toList();
    }
}
