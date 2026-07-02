package com.donutpank.bank.exchangerate;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.donutpank.bank.security.CurrentUser;
import com.donutpank.bank.security.JwtService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExchangeRateController.class)
class ExchangeRateControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ExchangeRateRepository exchangeRateRepository;
    @MockitoBean JwtService jwtService;

    private static final CurrentUser CURRENT_USER = new CurrentUser(1L, "alice");

    private static UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(
                CURRENT_USER, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void listRates_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/exchange-rates"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listRates_authenticated_returnsRates() throws Exception {
        when(exchangeRateRepository.findAllByOrderByBaseCurrencyCodeAscQuoteCurrencyCodeAsc())
                .thenReturn(List.of());

        mockMvc.perform(get("/exchange-rates").with(authentication(auth())))
                .andExpect(status().isOk());
    }
}
