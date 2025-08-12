package org.mifos.loanrisk.external.credit;

import org.mifos.loanrisk.domain.Aggregator;
import reactor.core.publisher.Mono;

/**
 * Interface for pulling credit bureau reports from third party providers.
 * Concrete implementations should encapsulate provider specific details
 * and be registered as Spring beans so they can be selected at runtime.
 */
public interface CreditBureauService {

    /**
     * @return unique provider name.
     */
    String getName();

    /**
     * Initiates a credit bureau pull for the given loan/aggregator.
     */
    Mono<Void> pullReport(Aggregator aggregator);
}

