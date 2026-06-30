package com.donutpank.bank.paymentorder;

import com.donutpank.bank.security.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentOrderController {

    private final PaymentOrderService paymentOrderService;

    public PaymentOrderController(PaymentOrderService paymentOrderService) {
        this.paymentOrderService = paymentOrderService;
    }

    @GetMapping("/payment-orders/{paymentOrderId}")
    public PaymentOrderDetail getPaymentOrder(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable Long paymentOrderId) {
        return paymentOrderService.getPaymentOrder(currentUser.id(), paymentOrderId);
    }
}
