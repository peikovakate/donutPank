package com.donutpank.bank.account;

import com.donutpank.bank.common.BadRequestException;
import com.donutpank.bank.common.NotFoundException;
import com.donutpank.bank.currency.Currency;
import com.donutpank.bank.currency.CurrencyRepository;
import com.donutpank.bank.ledger.LedgerRepository;
import com.donutpank.bank.user.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CurrencyRepository currencyRepository;
    private final UserRepository userRepository;
    private final LedgerRepository ledgerRepository;

    public AccountService(AccountRepository accountRepository, CurrencyRepository currencyRepository,
                           UserRepository userRepository, LedgerRepository ledgerRepository) {
        this.accountRepository = accountRepository;
        this.currencyRepository = currencyRepository;
        this.userRepository = userRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listAccounts(Long userId) {
        return accountRepository.findByUserIdOrderByIdAsc(userId).stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {
        Currency currency = currencyRepository.findById(request.currency().name())
                .filter(Currency::isActive)
                .orElseThrow(() -> new BadRequestException("Unsupported currency: " + request.currency()));

        Account account = accountRepository.save(Account.builder()
                .user(userRepository.getReferenceById(userId))
                .currency(currency)
                .balance(BigDecimal.ZERO.setScale(4))
                .build());
        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long userId, Long accountId) {
        return AccountResponse.from(findOwnedAccount(userId, accountId));
    }

    @Transactional(readOnly = true)
    public List<BalancePoint> getBalanceHistory(Long userId, Long accountId, Instant from, Instant to) {
        Account account = findOwnedAccount(userId, accountId);
        Instant rangeStart = from != null ? from : account.getCreatedAt();
        Instant rangeEnd = to != null ? to : Instant.now();

        return ledgerRepository.findBalanceHistory(accountId, rangeStart, rangeEnd).stream()
                .map(entry -> new BalancePoint(entry.getCreatedAt(), entry.getBalanceAfter().toPlainString()))
                .toList();
    }

    private Account findOwnedAccount(Long userId, Long accountId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }
}
