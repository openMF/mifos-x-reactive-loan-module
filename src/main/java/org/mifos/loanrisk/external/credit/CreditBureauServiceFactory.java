package org.mifos.loanrisk.external.credit;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory for selecting a {@link CreditBureauService} implementation at
 * runtime. Currently only placeholder services may exist.
 */
@Component
@RequiredArgsConstructor
public class CreditBureauServiceFactory {

    private final List<CreditBureauService> services;

    @Value("${credit.provider:}")
    private String provider;

    public CreditBureauService getService() {
        return services.stream().filter(s -> s.getName().equalsIgnoreCase(provider)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No CreditBureauService for provider " + provider));
    }
}

