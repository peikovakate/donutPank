package com.donutpank.bank.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.donutpank.bank.account.Account;
import com.donutpank.bank.account.AccountRepository;
import com.donutpank.bank.currency.Currency;
import com.donutpank.bank.currency.CurrencyRepository;
import com.donutpank.bank.user.User;
import com.donutpank.bank.paymentorder.PaymentOrder;
import com.donutpank.bank.paymentorder.PaymentOrderRepository;
import com.donutpank.bank.paymentorder.PaymentOrderStatus;
import com.donutpank.bank.paymentorder.PaymentOrderType;
import com.donutpank.bank.user.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class PaymentOrderRepositoryTest {

    @Autowired
    private PaymentOrderRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private User owner;
    private User stranger;
    private Account ownerSourceAccount;
    private Account ownerDestinationAccount;

    @BeforeEach
    void setUp() {
        Currency eur = currencyRepository.save(
                Currency.builder().code("EUR").name("Euro").minorUnit((short) 2).active(true).build());
        Currency usd = currencyRepository.save(
                Currency.builder().code("USD").name("US Dollar").minorUnit((short) 2).active(true).build());
        owner = userRepository.save(User.builder().username("owner").passwordHash("hash").build());
        stranger = userRepository.save(User.builder().username("stranger").passwordHash("hash").build());
        ownerSourceAccount =
                accountRepository.save(Account.builder().user(owner).currency(eur).balance(BigDecimal.TEN).build());
        ownerDestinationAccount =
                accountRepository.save(Account.builder().user(owner).currency(usd).balance(BigDecimal.TEN).build());
    }

    @Test
    void findByIdempotencyKeyFindsTheOriginalRowAndNothingForAnUnknownKey() {
        PaymentOrder transaction = transactionRepository.save(PaymentOrder.builder()
                .type(PaymentOrderType.DEBIT)
                .account(ownerSourceAccount)
                .amount(new BigDecimal("5.0000"))
                .status(PaymentOrderStatus.COMPLETED)
                .idempotencyKey("key-1")
                .build());

        assertThat(transactionRepository.findByIdempotencyKey("key-1"))
                .get()
                .extracting(PaymentOrder::getId)
                .isEqualTo(transaction.getId());
        assertThat(transactionRepository.findByIdempotencyKey("missing-key")).isEmpty();
    }

    @Test
    void findByIdAndOwnerMatchesEitherSourceOrDestinationAccountOwnerOnly() {
        PaymentOrder exchange = transactionRepository.save(PaymentOrder.builder()
                .type(PaymentOrderType.EXCHANGE)
                .account(ownerSourceAccount)
                .toAccount(ownerDestinationAccount)
                .amount(new BigDecimal("5.0000"))
                .status(PaymentOrderStatus.COMPLETED)
                .idempotencyKey("key-2")
                .build());

        assertThat(transactionRepository.findByIdAndOwner(exchange.getId(), owner.getId())).isPresent();
        assertThat(transactionRepository.findByIdAndOwner(exchange.getId(), stranger.getId())).isEmpty();
    }
}
