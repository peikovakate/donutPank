package com.donutpank.bank.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.donutpank.bank.currency.Currency;
import com.donutpank.bank.user.User;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AccountTest {

    private Account accountWithBalance(String balance) {
        return Account.builder()
                .user(User.builder().username("alice").passwordHash("hash").build())
                .currency(Currency.builder().code("EUR").name("Euro").minorUnit((short) 2).active(true).build())
                .balance(new BigDecimal(balance))
                .build();
    }

    @Test
    void creditIncreasesBalance() {
        Account account = accountWithBalance("100.0000");

        account.credit(new BigDecimal("25.5000"));

        assertThat(account.getBalance()).isEqualByComparingTo("125.5000");
    }

    @Test
    void debitDecreasesBalance() {
        Account account = accountWithBalance("100.0000");

        account.debit(new BigDecimal("40.0000"));

        assertThat(account.getBalance()).isEqualByComparingTo("60.0000");
    }

    @Test
    void debitDownToExactlyZeroIsAllowed() {
        Account account = accountWithBalance("50.0000");

        account.debit(new BigDecimal("50.0000"));

        assertThat(account.getBalance()).isEqualByComparingTo("0.0000");
    }

    @Test
    void debitBeyondBalanceThrowsInsufficientFundsAndLeavesBalanceUnchanged() {
        Account account = accountWithBalance("10.0000");

        assertThatThrownBy(() -> account.debit(new BigDecimal("10.0001")))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(account.getBalance()).isEqualByComparingTo("10.0000");
    }
}
