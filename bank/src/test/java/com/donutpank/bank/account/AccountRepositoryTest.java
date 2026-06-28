package com.donutpank.bank.account;

import static org.assertj.core.api.Assertions.assertThat;

import com.donutpank.bank.currency.Currency;
import com.donutpank.bank.currency.CurrencyRepository;
import com.donutpank.bank.user.User;
import com.donutpank.bank.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private User owner;
    private User intruder;
    private Currency eur;

    @BeforeEach
    void setUp() {
        eur = currencyRepository.save(
                Currency.builder().code("EUR").name("Euro").minorUnit((short) 2).active(true).build());
        owner = userRepository.save(User.builder().username("owner").passwordHash("hash").build());
        intruder = userRepository.save(User.builder().username("intruder").passwordHash("hash").build());
    }

    private Account newAccount(User user, String balance) {
        return accountRepository.save(Account.builder()
                .user(user)
                .currency(eur)
                .balance(new BigDecimal(balance))
                .build());
    }

    @Test
    void findByUserIdOrderByIdAscReturnsOnlyThatUsersAccountsInOrder() {
        Account first = newAccount(owner, "10.0000");
        Account second = newAccount(owner, "20.0000");
        newAccount(intruder, "999.0000");

        List<Account> accounts = accountRepository.findByUserIdOrderByIdAsc(owner.getId());

        assertThat(accounts).extracting(Account::getId).containsExactly(first.getId(), second.getId());
    }

    @Test
    void findByIdAndUserIdIsScopedToTheOwner() {
        Account account = newAccount(owner, "10.0000");

        assertThat(accountRepository.findByIdAndUserId(account.getId(), owner.getId())).isPresent();
        assertThat(accountRepository.findByIdAndUserId(account.getId(), intruder.getId())).isEmpty();
    }

    @Test
    void findByIdAndUserIdForUpdateIsScopedToTheOwnerAndLoadsTheRow() {
        Account account = newAccount(owner, "10.0000");

        assertThat(accountRepository.findByIdAndUserIdForUpdate(account.getId(), owner.getId()))
                .get()
                .extracting(Account::getBalance)
                .isEqualTo(new BigDecimal("10.0000"));
        assertThat(accountRepository.findByIdAndUserIdForUpdate(account.getId(), intruder.getId())).isEmpty();
    }
}
