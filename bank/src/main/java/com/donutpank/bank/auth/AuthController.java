package com.donutpank.bank.auth;

import com.donutpank.bank.security.CurrentUser;
import com.donutpank.bank.user.UserResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/users/me")
    public UserResponse me(@AuthenticationPrincipal CurrentUser currentUser) {
        return new UserResponse(currentUser.id(), currentUser.username());
    }
}
