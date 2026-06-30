package com.donutpank.bank.moneymovement;

import com.donutpank.bank.config.ExternalCallProperties;
import java.net.SocketTimeoutException;
import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Simulates the external call via httpstat.us, per the assignment's "external logging" example. */
@Component
public class HttpStatPreDebitCallClient implements PreDebitCallClient {

    private final RestClient restClient;

    public HttpStatPreDebitCallClient(ExternalCallProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofMillis(properties.timeoutMs());
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);

        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public ExternalCallOutcome call() {
        try {
            restClient.get().uri("/200").retrieve().toBodilessEntity();
            return ExternalCallOutcome.SUCCESS;
        } catch (ResourceAccessException e) {
            return e.getCause() instanceof SocketTimeoutException
                    ? ExternalCallOutcome.TIMEOUT
                    : ExternalCallOutcome.ERROR;
        } catch (RestClientException e) {
            return ExternalCallOutcome.ERROR;
        }
    }
}
