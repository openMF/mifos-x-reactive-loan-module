package org.mifos.loanrisk.external.bsa;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory that selects a {@link BankStatementAnalysisService} implementation
 * based on application configuration.
 */
@Component
@RequiredArgsConstructor
public class BankStatementAnalysisServiceFactory {

    private final List<BankStatementAnalysisService> services;

    /**
     * Name of the provider to use. Defaults to "arya" if not specified.
     */
    @Value("${bsa.provider:arya}")
    private String provider;

    /**
     * @return the service implementation matching the configured provider
     *         name.
     * @throws IllegalStateException if no matching provider is available
     */
    public BankStatementAnalysisService getService() {
        return services.stream().filter(s -> s.getName().equalsIgnoreCase(provider)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No BankStatementAnalysisService for provider " + provider));
    }
}

