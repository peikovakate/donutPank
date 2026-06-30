package com.donutpank.bank.auth;

import com.donutpank.bank.security.CurrentUser;
import com.donutpank.bank.security.JwtService;
import com.donutpank.bank.user.User;
import com.donutpank.bank.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptLimiter loginAttemptLimiter;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        JwtService jwtService, LoginAttemptLimiter loginAttemptLimiter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginAttemptLimiter = loginAttemptLimiter;
    }

    public LoginResponse login(LoginRequest request) {
        if (loginAttemptLimiter.isLocked(request.username())) {
            throw new TooManyAttemptsException("Too many failed login attempts. Try again later.");
        }

        User user = userRepository.findByUsername(request.username()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttemptLimiter.recordFailure(request.username());
            throw new BadCredentialsException("Invalid username or password");
        }

        loginAttemptLimiter.recordSuccess(request.username());
        String token = jwtService.generateToken(new CurrentUser(user.getId(), user.getUsername()));
        return new LoginResponse(token);
    }
}
