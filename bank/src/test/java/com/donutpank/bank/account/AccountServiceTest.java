package com.donutpank.bank.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.donutpank.bank.common.BadRequestException;
import com.donutpank.bank.common.NotFoundException;
import com.donutpank.bank.currency.Currency;
import com.donutpank.bank.currency.CurrencyCode;
import com.donutpank.bank.currency.CurrencyRepository;
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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class AccountServiceTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private PaymentOrderRepository transactionRepository;

    private AccountService service;
    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        service = new AccountService(accountRepository, currencyRepository, userRepository, ledgerRepository);

        currencyRepository.save(Currency.builder().code("EUR").name("Euro").minorUnit((short) 2).active(true).build());
        currencyRepository.save(Currency.builder().code("USD").name("US Dollar").minorUnit((short) 2).active(true).build());

        user = userRepository.save(User.builder().username("alice").passwordHash("hash").build());
        otherUser = userRepository.save(User.builder().username("bob").passwordHash("hash").build());
    }

    @Test
    void createsAccountWithZeroBalance() {
        AccountResponse response = service.createAccount(user.getId(), new CreateAccountRequest(CurrencyCode.EUR));

        assertThat(response.currency()).isEqualTo("EUR");
        assertThat(response.balance()).isEqualTo("0.0000");
    }

    @Test
    void rejectsCurrencyThatHasNotBeenSeeded() {
        assertThatThrownBy(() -> service.createAccount(user.getId(), new CreateAccountRequest(CurrencyCode.VND)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void listAccountsOnlyReturnsCallersAccounts() {
        service.createAccount(user.getId(), new CreateAccountRequest(CurrencyCode.EUR));
        service.createAccount(otherUser.getId(), new CreateAccountRequest(CurrencyCode.USD));

        assertThat(service.listAccounts(user.getId())).hasSize(1);
    }

    @Test
    void getAccountThrowsNotFoundForAnotherUsersAccount() {
        AccountResponse created = service.createAccount(otherUser.getId(), new CreateAccountRequest(CurrencyCode.EUR));

        assertThatThrownBy(() -> service.getAccount(user.getId(), created.id()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void balanceHistoryReturnsLedgerPointsWithinRange() {
        AccountResponse created = service.createAccount(user.getId(), new CreateAccountRequest(CurrencyCode.EUR));
        Account account = accountRepository.findById(created.id()).orElseThrow();

        PaymentOrder paymentOrder = transactionRepository.save(PaymentOrder.builder()
                .type(PaymentOrderType.CREDIT)
                .account(account)
                .amount(new BigDecimal("100.0000"))
                .status(PaymentOrderStatus.COMPLETED)
                .idempotencyKey("key-1")
                .build());
        ledgerRepository.save(LedgerEntry.builder()
                .paymentOrder(paymentOrder)
                .account(account)
                .direction(Direction.CREDIT)
                .amount(new BigDecimal("100.0000"))
                .balanceAfter(new BigDecimal("100.0000"))
                .build());

        List<BalancePoint> points = service.getBalanceHistory(user.getId(), created.id(), null, null);

        assertThat(points).hasSize(1);
        assertThat(points.get(0).balance()).isEqualTo("100.0000");
    }
}
