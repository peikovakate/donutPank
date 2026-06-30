package com.donutpank.bank.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import com.donutpank.bank.account.Account;
import com.donutpank.bank.account.AccountRepository;
import com.donutpank.bank.currency.Currency;
import com.donutpank.bank.currency.CurrencyRepository;
import com.donutpank.bank.paymentorder.PaymentOrder;
import com.donutpank.bank.paymentorder.PaymentOrderRepository;
import com.donutpank.bank.paymentorder.PaymentOrderStatus;
import com.donutpank.bank.paymentorder.PaymentOrderType;
import com.donutpank.bank.user.User;
import com.donutpank.bank.user.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class LedgerRepositoryTest {

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private PaymentOrderRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private Account account;
    private Account otherAccount;

    @BeforeEach
    void setUp() {
        Currency eur = currencyRepository.save(
                Currency.builder().code("EUR").name("Euro").minorUnit((short) 2).active(true).build());
        User user = userRepository.save(User.builder().username("owner").passwordHash("hash").build());
        account = accountRepository.save(
                Account.builder().user(user).currency(eur).balance(BigDecimal.ZERO).build());
        otherAccount = accountRepository.save(
                Account.builder().user(user).currency(eur).balance(BigDecimal.ZERO).build());
    }

    private LedgerEntry creditEntry(Account targetAccount, String amount, String balanceAfter) {
        PaymentOrder transaction = transactionRepository.save(PaymentOrder.builder()
                .type(PaymentOrderType.CREDIT)
                .account(targetAccount)
                .amount(new BigDecimal(amount))
                .status(PaymentOrderStatus.COMPLETED)
                .idempotencyKey(UUID.randomUUID().toString())
                .build());
        return ledgerRepository.save(LedgerEntry.builder()
                .paymentOrder(transaction)
                .account(targetAccount)
                .direction(Direction.CREDIT)
                .amount(new BigDecimal(amount))
                .balanceAfter(new BigDecimal(balanceAfter))
                .build());
    }

    @Test
    void findFirstPageReturnsNewestFirstAndOnlyForThatAccount() {
        creditEntry(otherAccount, "999.0000", "999.0000");
        creditEntry(account, "10.0000", "10.0000");
        LedgerEntry second = creditEntry(account, "5.0000", "15.0000");
        LedgerEntry third = creditEntry(account, "5.0000", "20.0000");

        List<LedgerEntry> page = ledgerRepository.findFirstPage(account.getId(), PageRequest.ofSize(2));

        assertThat(page).extracting(LedgerEntry::getId).containsExactly(third.getId(), second.getId());
    }

    @Test
    void findNextPageWalksTheRemainingHistoryOneCursorAtATime() {
        LedgerEntry first = creditEntry(account, "10.0000", "10.0000");
        LedgerEntry second = creditEntry(account, "5.0000", "15.0000");
        LedgerEntry third = creditEntry(account, "5.0000", "20.0000");

        List<LedgerEntry> firstPage = ledgerRepository.findFirstPage(account.getId(), PageRequest.ofSize(1));
        assertThat(firstPage).extracting(LedgerEntry::getId).containsExactly(third.getId());

        LedgerEntry cursor1 = firstPage.get(0);
        List<LedgerEntry> secondPage = ledgerRepository.findNextPage(
                account.getId(), cursor1.getCreatedAt(), cursor1.getId(), PageRequest.ofSize(1));
        assertThat(secondPage).extracting(LedgerEntry::getId).containsExactly(second.getId());

        LedgerEntry cursor2 = secondPage.get(0);
        List<LedgerEntry> thirdPage = ledgerRepository.findNextPage(
                account.getId(), cursor2.getCreatedAt(), cursor2.getId(), PageRequest.ofSize(1));
        assertThat(thirdPage).extracting(LedgerEntry::getId).containsExactly(first.getId());

        LedgerEntry cursor3 = thirdPage.get(0);
        List<LedgerEntry> fourthPage = ledgerRepository.findNextPage(
                account.getId(), cursor3.getCreatedAt(), cursor3.getId(), PageRequest.ofSize(1));
        assertThat(fourthPage).isEmpty();
    }

    @Test
    void findBalanceHistoryReturnsEntriesWithinRangeOldestFirst() {
        LedgerEntry first = creditEntry(account, "10.0000", "10.0000");
        LedgerEntry second = creditEntry(account, "5.0000", "15.0000");

        Instant from = first.getCreatedAt().minusSeconds(1);
        Instant to = second.getCreatedAt().plusSeconds(1);

        List<LedgerEntry> history = ledgerRepository.findBalanceHistory(account.getId(), from, to);

        assertThat(history).extracting(LedgerEntry::getId).containsExactly(first.getId(), second.getId());
    }
}
