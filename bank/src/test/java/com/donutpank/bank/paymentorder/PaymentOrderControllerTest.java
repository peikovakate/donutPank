package com.donutpank.bank.paymentorder;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.donutpank.bank.common.NotFoundException;
import com.donutpank.bank.security.CurrentUser;
import com.donutpank.bank.security.JwtService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentOrderController.class)
class PaymentOrderControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean PaymentOrderService paymentOrderService;
    @MockitoBean JwtService jwtService;

    private static final CurrentUser CURRENT_USER = new CurrentUser(1L, "alice");
    private static final PaymentOrderDetail SAMPLE_DETAIL = new PaymentOrderDetail(
            42L, PaymentOrderType.CREDIT, 10L, null, "100.0000",
            PaymentOrderStatus.COMPLETED, null, null, Instant.EPOCH, List.of());

    private static UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(
                CURRENT_USER, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void getPaymentOrder_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/payment-orders/42"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPaymentOrder_authenticated_returns200() throws Exception {
        when(paymentOrderService.getPaymentOrder(1L, 42L)).thenReturn(SAMPLE_DETAIL);

        mockMvc.perform(get("/payment-orders/42").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getPaymentOrder_notFound_returns404() throws Exception {
        when(paymentOrderService.getPaymentOrder(1L, 99L))
                .thenThrow(new NotFoundException("Payment order not found"));

        mockMvc.perform(get("/payment-orders/99").with(authentication(auth())))
                .andExpect(status().isNotFound());
    }
}
