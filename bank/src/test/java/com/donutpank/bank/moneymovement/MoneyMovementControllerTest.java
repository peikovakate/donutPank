package com.donutpank.bank.moneymovement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.donutpank.bank.paymentorder.PaymentOrderResponse;
import com.donutpank.bank.paymentorder.PaymentOrderStatus;
import com.donutpank.bank.paymentorder.PaymentOrderType;
import com.donutpank.bank.security.CurrentUser;
import com.donutpank.bank.security.JwtService;
import com.donutpank.bank.testsupport.SecurityTestConfig;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MoneyMovementController.class)
@Import(SecurityTestConfig.class)
class MoneyMovementControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean MoneyMovementService moneyMovementService;
    @MockitoBean JwtService jwtService;

    private static final CurrentUser CURRENT_USER = new CurrentUser(1L, "alice");
    private static final PaymentOrderResponse COMPLETED_RESPONSE = new PaymentOrderResponse(
            99L, PaymentOrderType.CREDIT, 10L, null, "100.0000",
            PaymentOrderStatus.COMPLETED, null, null, Instant.EPOCH);

    private static UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(
                CURRENT_USER, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void credit_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/accounts/10/credits")
                        .header("Idempotency-Key", "key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":"100"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void credit_missingIdempotencyKey_returns400() throws Exception {
        mockMvc.perform(post("/accounts/10/credits")
                        .with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":"100"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void credit_valid_returns201() throws Exception {
        when(moneyMovementService.credit(eq(1L), eq(10L), eq("key-1"), any())).thenReturn(COMPLETED_RESPONSE);

        mockMvc.perform(post("/accounts/10/credits")
                        .with(authentication(auth()))
                        .header("Idempotency-Key", "key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":"100"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void debit_valid_returns201() throws Exception {
        PaymentOrderResponse debitResponse = new PaymentOrderResponse(
                99L, PaymentOrderType.DEBIT, 10L, null, "50.0000",
                PaymentOrderStatus.COMPLETED, null, null, Instant.EPOCH);
        when(moneyMovementService.debit(eq(1L), eq(10L), eq("key-2"), any())).thenReturn(debitResponse);

        mockMvc.perform(post("/accounts/10/debits")
                        .with(authentication(auth()))
                        .header("Idempotency-Key", "key-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":"50"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void exchange_valid_returns201() throws Exception {
        PaymentOrderResponse exchangeResponse = new PaymentOrderResponse(
                99L, PaymentOrderType.EXCHANGE, 10L, 20L, "100.0000",
                PaymentOrderStatus.COMPLETED, null, null, Instant.EPOCH);
        when(moneyMovementService.exchange(eq(1L), eq("key-3"), any())).thenReturn(exchangeResponse);

        mockMvc.perform(post("/exchanges")
                        .with(authentication(auth()))
                        .header("Idempotency-Key", "key-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountId":10,"toAccountId":20,"fromAmount":"100"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.toAccountId").value(20));
    }
}
