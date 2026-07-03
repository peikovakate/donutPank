package com.donutpank.bank.account;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.donutpank.bank.common.NotFoundException;
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

@WebMvcTest(AccountController.class)
@Import(SecurityTestConfig.class)
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AccountService accountService;
    @MockitoBean JwtService jwtService;

    private static final CurrentUser CURRENT_USER = new CurrentUser(1L, "alice");
    private static final AccountResponse SAMPLE_ACCOUNT =
            new AccountResponse(10L, "EUR", "0.0000", Instant.EPOCH);

    private static UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(
                CURRENT_USER, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void listAccounts_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listAccounts_authenticated_returnsAccountList() throws Exception {
        when(accountService.listAccounts(1L)).thenReturn(List.of(SAMPLE_ACCOUNT));

        mockMvc.perform(get("/accounts").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currency").value("EUR"));
    }

    @Test
    void createAccount_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/accounts")
                        .with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_validBody_returns201() throws Exception {
        when(accountService.createAccount(eq(1L), any())).thenReturn(SAMPLE_ACCOUNT);

        mockMvc.perform(post("/accounts")
                        .with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currency":"EUR"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void getAccount_authenticated_returns200() throws Exception {
        when(accountService.getAccount(1L, 10L)).thenReturn(SAMPLE_ACCOUNT);

        mockMvc.perform(get("/accounts/10").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getAccount_notFound_returns404() throws Exception {
        when(accountService.getAccount(1L, 99L)).thenThrow(new NotFoundException("Account not found"));

        mockMvc.perform(get("/accounts/99").with(authentication(auth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBalanceHistory_authenticated_returns200() throws Exception {
        when(accountService.getBalanceHistory(eq(1L), eq(10L), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/accounts/10/balance-history").with(authentication(auth())))
                .andExpect(status().isOk());
    }
}
