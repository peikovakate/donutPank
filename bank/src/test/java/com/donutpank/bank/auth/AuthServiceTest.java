package com.donutpank.bank.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.donutpank.bank.security.CurrentUser;
import com.donutpank.bank.security.JwtService;
import com.donutpank.bank.user.User;
import com.donutpank.bank.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock LoginAttemptLimiter loginAttemptLimiter;
    @InjectMocks AuthService authService;

    @Test
    void login_validCredentials_returnsToken() {
        User user = User.builder().username("alice").passwordHash("hash").build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtService.generateToken(any(CurrentUser.class))).thenReturn("jwt-token");

        LoginResponse response = authService.login(new LoginRequest("alice", "secret"));

        assertThat(response.token()).isEqualTo("jwt-token");
        verify(loginAttemptLimiter).recordSuccess("alice");
    }

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        User user = User.builder().username("alice").passwordHash("hash").build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
        verify(loginAttemptLimiter).recordFailure("alice");
    }

    @Test
    void login_unknownUser_throwsBadCredentials() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "any")))
                .isInstanceOf(BadCredentialsException.class);
        verify(loginAttemptLimiter).recordFailure("alice");
    }

    @Test
    void login_accountLocked_throwsTooManyAttempts() {
        when(loginAttemptLimiter.isLocked("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "any")))
                .isInstanceOf(TooManyAttemptsException.class);
        verifyNoInteractions(userRepository, jwtService);
    }
}
