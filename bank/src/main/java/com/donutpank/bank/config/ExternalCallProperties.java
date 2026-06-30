package com.donutpank.bank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.external-call")
public record ExternalCallProperties(String baseUrl, long timeoutMs) {
}
