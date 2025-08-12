package org.mifos.loanrisk.external.ekyc;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EkycVerificationServiceFactory {

    private final List<EkycVerificationService> services;

    @Value("${ekyc.provider:}")
    private String provider;

    public EkycVerificationService getService() {
        return services.stream().filter(s -> s.getName().equalsIgnoreCase(provider)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No EkycVerificationService for provider " + provider));
    }
}

