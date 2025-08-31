package org.mifos.loanrisk.external.cb;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory that selects a {@link CreditBureauService} implementation based on configuration.
 */
@Component
@RequiredArgsConstructor
public class CreditBureauServiceFactory {

    private final List<CreditBureauService> services;

    /**
     * Name of the provider to use. Defaults to "isoftpull" if not specified.
     */
    @Value("${cb.provider:isoftpull}")
    private String provider;

    /**
     * @return the service implementation matching the configured provider name.
     * @throws IllegalStateException if no matching provider is available
     */
    public CreditBureauService getService() {
        return services.stream().filter(s -> s.getName().equalsIgnoreCase(provider)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No CreditBureauService for provider " + provider));
    }
}
