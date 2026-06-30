package com.donutpank.bank.paymentorder;

import com.donutpank.bank.common.NotFoundException;
import com.donutpank.bank.ledger.LedgerRepository;
import com.donutpank.bank.transaction.LedgerEntryResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentOrderService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final LedgerRepository ledgerRepository;

    public PaymentOrderService(PaymentOrderRepository paymentOrderRepository,
                                LedgerRepository ledgerRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @Transactional(readOnly = true)
    public PaymentOrderDetail getPaymentOrder(Long userId, Long paymentOrderId) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByIdAndOwner(paymentOrderId, userId)
                .orElseThrow(() -> new NotFoundException("Payment order not found"));

        List<LedgerEntryResponse> ledgerEntries = ledgerRepository
                .findByPaymentOrderId(paymentOrderId).stream()
                .map(LedgerEntryResponse::from)
                .toList();

        return PaymentOrderDetail.from(paymentOrder, ledgerEntries);
    }
}
