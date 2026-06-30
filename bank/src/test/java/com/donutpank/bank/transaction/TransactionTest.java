package com.donutpank.bank.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.donutpank.bank.account.Account;
import com.donutpank.bank.currency.Currency;
import com.donutpank.bank.paymentorder.PaymentOrder;
import com.donutpank.bank.paymentorder.ReasonCode;
import com.donutpank.bank.paymentorder.PaymentOrderStatus;
import com.donutpank.bank.paymentorder.PaymentOrderType;
import com.donutpank.bank.user.User;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TransactionTest {

    private PaymentOrder pendingDebit() {
        Account account = Account.builder()
                .user(User.builder().username("alice").passwordHash("hash").build())
                .currency(Currency.builder().code("EUR").name("Euro").minorUnit((short) 2).active(true).build())
                .balance(BigDecimal.TEN)
                .build();
        return PaymentOrder.builder()
                .type(PaymentOrderType.DEBIT)
                .account(account)
                .amount(new BigDecimal("5.0000"))
                .status(PaymentOrderStatus.PENDING)
                .build();
    }

    @Test
    void markCompletedSetsStatusAndClearsReasonCode() {
        PaymentOrder transaction = pendingDebit();

        transaction.markCompleted();

        assertThat(transaction.getStatus()).isEqualTo(PaymentOrderStatus.COMPLETED);
        assertThat(transaction.getReasonCode()).isNull();
    }

    @Test
    void markFailedSetsStatusAndReasonCode() {
        PaymentOrder transaction = pendingDebit();

        transaction.markFailed(ReasonCode.EXTERNAL_CALL_TIMEOUT);

        assertThat(transaction.getStatus()).isEqualTo(PaymentOrderStatus.FAILED);
        assertThat(transaction.getReasonCode()).isEqualTo(ReasonCode.EXTERNAL_CALL_TIMEOUT);
    }

    @Test
    void markRejectedSetsStatusAndReasonCode() {
        PaymentOrder transaction = pendingDebit();

        transaction.markRejected(ReasonCode.INSUFFICIENT_FUNDS);

        assertThat(transaction.getStatus()).isEqualTo(PaymentOrderStatus.REJECTED);
        assertThat(transaction.getReasonCode()).isEqualTo(ReasonCode.INSUFFICIENT_FUNDS);
    }
}
