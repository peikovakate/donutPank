package com.donutpank.bank.paymentorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.donutpank.bank.account.Account;
import com.donutpank.bank.common.NotFoundException;
import com.donutpank.bank.ledger.LedgerRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentOrderServiceTest {

    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock LedgerRepository ledgerRepository;
    @InjectMocks PaymentOrderService service;

    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 42L;

    @Test
    void getPaymentOrder_notOwned_throwsNotFound() {
        when(paymentOrderRepository.findByIdAndOwner(ORDER_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPaymentOrder(USER_ID, ORDER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getPaymentOrder_owned_returnsDetailWithLedgerEntries() {
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(10L);

        PaymentOrder order = mock(PaymentOrder.class);
        when(order.getId()).thenReturn(ORDER_ID);
        when(order.getType()).thenReturn(PaymentOrderType.CREDIT);
        when(order.getAccount()).thenReturn(account);
        when(order.getToAccount()).thenReturn(null);
        when(order.getAmount()).thenReturn(new BigDecimal("100.0000"));
        when(order.getStatus()).thenReturn(PaymentOrderStatus.COMPLETED);
        when(order.getReasonCode()).thenReturn(null);
        when(order.getDescription()).thenReturn(null);
        when(order.getUpdatedAt()).thenReturn(null);

        when(paymentOrderRepository.findByIdAndOwner(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        when(ledgerRepository.findByPaymentOrderId(ORDER_ID)).thenReturn(List.of());

        PaymentOrderDetail detail = service.getPaymentOrder(USER_ID, ORDER_ID);

        assertThat(detail.id()).isEqualTo(ORDER_ID);
        assertThat(detail.status()).isEqualTo(PaymentOrderStatus.COMPLETED);
        assertThat(detail.ledgerEntries()).isEmpty();
    }
}
