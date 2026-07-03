package com.donutpank.bank.testsupport;

import com.donutpank.bank.config.CorsProperties;
import com.donutpank.bank.config.SecurityConfig;
import com.donutpank.bank.security.ProblemDetailAccessDeniedHandler;
import com.donutpank.bank.security.ProblemDetailAuthEntryPoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({SecurityConfig.class, ProblemDetailAuthEntryPoint.class, ProblemDetailAccessDeniedHandler.class})
@EnableConfigurationProperties(CorsProperties.class)
public class SecurityTestConfig {
}
