package com.donutpank.bank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.login")
public record LoginProperties(int maxAttempts, long lockoutMinutes) {
}
