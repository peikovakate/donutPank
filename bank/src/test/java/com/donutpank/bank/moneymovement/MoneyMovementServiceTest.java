package com.donutpank.bank.moneymovement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.donutpank.bank.account.Account;
import com.donutpank.bank.account.AccountRepository;
import com.donutpank.bank.common.BadRequestException;
import com.donutpank.bank.common.NotFoundException;
import com.donutpank.bank.currency.Currency;
import com.donutpank.bank.paymentorder.PaymentOrder;
import com.donutpank.bank.paymentorder.PaymentOrderResponse;
import com.donutpank.bank.paymentorder.PaymentOrderStatus;
import com.donutpank.bank.paymentorder.PaymentOrderType;
import com.donutpank.bank.paymentorder.ReasonCode;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MoneyMovementServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock PaymentOrderAttemptRecorder attemptRecorder;
    @Mock MoneyMovementExecutor executor;
    @Mock PreDebitCallClient preDebitCallClient;
    @InjectMocks MoneyMovementService service;

    private static final Long USER_ID = 1L;
    private static final Long ACCOUNT_ID = 10L;

    private Account mockAccount(Long id, String currencyCode) {
        Account account = mock(Account.class);
        Currency currency = mock(Currency.class);
        when(account.getId()).thenReturn(id);
        when(currency.getCode()).thenReturn(currencyCode);
        when(account.getCurrency()).thenReturn(currency);
        return account;
    }

    private PaymentOrder pendingOrder(Long id) {
        PaymentOrder order = mock(PaymentOrder.class);
        when(order.getId()).thenReturn(id);
        when(order.getStatus()).thenReturn(PaymentOrderStatus.PENDING);
        return order;
    }

    private PaymentOrder completedOrder() {
        PaymentOrder order = mock(PaymentOrder.class);
        when(order.getStatus()).thenReturn(PaymentOrderStatus.COMPLETED);
        when(order.getAccount()).thenReturn(mockAccount(ACCOUNT_ID, "EUR"));
        when(order.getAmount()).thenReturn(new BigDecimal("100.0000"));
        return order;
    }

    // --- credit ---

    @Test
    void credit_accountNotOwnedByUser_throwsNotFound() {
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.credit(USER_ID, ACCOUNT_ID, "key-1", new CreditRequest("100", null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void credit_terminalIdempotentOrder_returnsExisting() {
        Account account = mockAccount(ACCOUNT_ID, "EUR");
        PaymentOrder existing = completedOrder();
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));
        when(attemptRecorder.insert(any(), any(), any(), any(), eq("key-1"), any())).thenReturn(existing);

        PaymentOrderResponse response = service.credit(USER_ID, ACCOUNT_ID, "key-1", new CreditRequest("100", null));

        assertThat(response.status()).isEqualTo(PaymentOrderStatus.COMPLETED);
    }

    @Test
    void credit_newOrder_delegatesToExecutor() {
        Account account = mockAccount(ACCOUNT_ID, "EUR");
        PaymentOrder pending = pendingOrder(99L);
        PaymentOrder completed = completedOrder();
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));
        when(attemptRecorder.insert(any(), any(), any(), any(), eq("key-1"), any())).thenReturn(pending);
        when(executor.completeCredit(99L, ACCOUNT_ID, USER_ID, new BigDecimal("100"))).thenReturn(completed);

        service.credit(USER_ID, ACCOUNT_ID, "key-1", new CreditRequest("100", null));

        verify(executor).completeCredit(99L, ACCOUNT_ID, USER_ID, new BigDecimal("100"));
    }

    // --- debit ---

    @Test
    void debit_accountNotOwnedByUser_throwsNotFound() {
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.debit(USER_ID, ACCOUNT_ID, "key-1", new DebitRequest("50", null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void debit_preDebitCallSucceeds_callsCompleteDebit() {
        Account account = mockAccount(ACCOUNT_ID, "EUR");
        PaymentOrder pending = pendingOrder(99L);
        PaymentOrder completed = completedOrder();
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));
        when(attemptRecorder.insert(any(), any(), any(), any(), any(), any())).thenReturn(pending);
        when(preDebitCallClient.call()).thenReturn(ExternalCallOutcome.SUCCESS);
        when(executor.completeDebit(99L, ACCOUNT_ID, USER_ID, new BigDecimal("50"))).thenReturn(completed);

        service.debit(USER_ID, ACCOUNT_ID, "key-1", new DebitRequest("50", null));

        verify(executor).completeDebit(99L, ACCOUNT_ID, USER_ID, new BigDecimal("50"));
    }

    @Test
    void debit_preDebitCallTimesOut_failsTransaction() {
        Account account = mockAccount(ACCOUNT_ID, "EUR");
        PaymentOrder pending = pendingOrder(99L);
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));
        when(attemptRecorder.insert(any(), any(), any(), any(), any(), any())).thenReturn(pending);
        when(preDebitCallClient.call()).thenReturn(ExternalCallOutcome.TIMEOUT);

        service.debit(USER_ID, ACCOUNT_ID, "key-1", new DebitRequest("50", null));

        verify(executor).failTransaction(99L, ReasonCode.EXTERNAL_CALL_TIMEOUT);
    }

    @Test
    void debit_preDebitCallErrors_failsTransaction() {
        Account account = mockAccount(ACCOUNT_ID, "EUR");
        PaymentOrder pending = pendingOrder(99L);
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));
        when(attemptRecorder.insert(any(), any(), any(), any(), any(), any())).thenReturn(pending);
        when(preDebitCallClient.call()).thenReturn(ExternalCallOutcome.ERROR);

        service.debit(USER_ID, ACCOUNT_ID, "key-1", new DebitRequest("50", null));

        verify(executor).failTransaction(99L, ReasonCode.EXTERNAL_CALL_ERROR);
    }

    // --- exchange ---

    @Test
    void exchange_fromAccountNotOwned_throwsNotFound() {
        when(accountRepository.findByIdAndUserId(10L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.exchange(USER_ID, "key-1", new ExchangeRequest(10L, 20L, "100")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void exchange_sameCurrency_throwsBadRequest() {
        Account from = mockAccount(10L, "EUR");
        Account to = mockAccount(20L, "EUR");
        when(accountRepository.findByIdAndUserId(10L, USER_ID)).thenReturn(Optional.of(from));
        when(accountRepository.findByIdAndUserId(20L, USER_ID)).thenReturn(Optional.of(to));

        assertThatThrownBy(() -> service.exchange(USER_ID, "key-1", new ExchangeRequest(10L, 20L, "100")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void exchange_newOrder_delegatesToExecutor() {
        Account from = mockAccount(10L, "EUR");
        Account to = mockAccount(20L, "USD");
        PaymentOrder pending = pendingOrder(99L);
        PaymentOrder completed = completedOrder();
        when(accountRepository.findByIdAndUserId(10L, USER_ID)).thenReturn(Optional.of(from));
        when(accountRepository.findByIdAndUserId(20L, USER_ID)).thenReturn(Optional.of(to));
        when(attemptRecorder.insert(eq(PaymentOrderType.EXCHANGE), eq(from), eq(to), any(), eq("key-1"), any()))
                .thenReturn(pending);
        when(executor.completeExchange(99L, 10L, 20L, USER_ID, new BigDecimal("100"))).thenReturn(completed);

        service.exchange(USER_ID, "key-1", new ExchangeRequest(10L, 20L, "100"));

        verify(executor).completeExchange(99L, 10L, 20L, USER_ID, new BigDecimal("100"));
    }
}
